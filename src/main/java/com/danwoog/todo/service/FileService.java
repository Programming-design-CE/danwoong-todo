package com.danwoog.todo.service;

import com.danwoog.todo.domain.file.FileEntity;
import com.danwoog.todo.domain.file.Folder;
import com.danwoog.todo.domain.todogroup.TodoGroup;
import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.dto.file.FileDto.*;
import com.danwoog.todo.exception.CustomException.*;
import com.danwoog.todo.repository.*;
import com.danwoog.todo.repository.file.FileItemRepository;
import com.danwoog.todo.repository.file.FolderRepository;
import com.danwoog.todo.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {

    private final FolderRepository folderRepository;
    private final FileItemRepository fileItemRepository;
    private final UserRepository userRepository;
    private final TodoGroupRepository todoGroupRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Transactional
    public FolderResponse getRootFolder(Long groupId, Long userId) {
        TodoGroup group = findGroup(groupId);
        User user = findUser(userId);
        Folder root = folderRepository.findByGroup_GroupIdAndParentFolderIdIsNull(groupId)
                .orElseGet(() -> folderRepository.save(
                        Folder.builder().group(group).folderName("기본 폴더")
                                .parentFolderId(null).createdBy(user).build()
                ));
        return toFolderResponse(root);
    }

    @Transactional
    public FolderResponse createSubFolder(Long groupId, Long parentFolderId,
                                          FolderCreateRequest request, Long userId) {
        TodoGroup group = findGroup(groupId);
        User user = findUser(userId);
        folderRepository.findById(parentFolderId)
                .orElseThrow(() -> new NotFoundException("상위 폴더가 존재하지 않습니다."));
        return toFolderResponse(folderRepository.save(
                Folder.builder().group(group).folderName(request.getFolderName())
                        .parentFolderId(parentFolderId).createdBy(user).build()
        ));
    }

    public FolderItemsResponse getFolderItems(Long groupId, Long folderId) {
        Folder current = folderRepository.findById(folderId)
                .orElseThrow(() -> new NotFoundException("폴더가 존재하지 않습니다."));
        List<FolderResponse> subFolders = folderRepository
                .findByGroup_GroupIdAndParentFolderId(groupId, folderId).stream()
                .map(this::toFolderResponse).collect(Collectors.toList());
        List<FileResponse> files = fileItemRepository
                .findByGroup_GroupIdAndFolder(groupId, current).stream()
                .map(this::toFileResponse).collect(Collectors.toList());
        return FolderItemsResponse.builder()
                .currentFolder(toFolderResponse(current))
                .folders(subFolders).files(files).build();
    }

    @Transactional
    public FileResponse uploadFile(Long groupId, Long folderId,
                                   MultipartFile file, Long userId) throws IOException {
        TodoGroup group = findGroup(groupId);
        User user = findUser(userId);
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new NotFoundException("폴더가 존재하지 않습니다."));

        String originalName = file.getOriginalFilename();
        String storedName = UUID.randomUUID() + "_" + originalName;
        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);
        Files.copy(file.getInputStream(), dir.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);

        return toFileResponse(fileItemRepository.save(
                new FileEntity(group, folder, user, originalName, storedName,
                        "/files/" + storedName, file.getSize(),
                        file.getContentType() != null ? file.getContentType() : "application/octet-stream")
        ));
    }

    public FileEntity getFile(Long fileId) {
        return fileItemRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("파일이 존재하지 않습니다."));
    }

    @Transactional
    public void deleteFile(Long fileId) throws IOException {
        FileEntity file = getFile(fileId);
        Files.deleteIfExists(Paths.get(uploadDir, file.getStoredName()));
        fileItemRepository.delete(file);
    }

    @Transactional
    public void deleteFolder(Long folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new NotFoundException("폴더가 존재하지 않습니다."));
        fileItemRepository.deleteAll(
                fileItemRepository.findByGroup_GroupIdAndFolder(folder.getGroup().getGroupId(), folder));
        folderRepository.findByGroup_GroupIdAndParentFolderId(folder.getGroup().getGroupId(), folderId)
                .forEach(sub -> deleteFolder(sub.getId()));
        folderRepository.delete(folder);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }

    private TodoGroup findGroup(Long groupId) {
        return todoGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("그룹을 찾을 수 없습니다."));
    }

    private FolderResponse toFolderResponse(Folder f) {
        return FolderResponse.builder().folderId(f.getId()).folderName(f.getFolderName())
                .parentFolderId(f.getParentFolderId()).createdAt(f.getCreatedAt()).build();
    }

    private FileResponse toFileResponse(FileEntity f) {
        return FileResponse.builder().fileId(f.getFileId()).originalName(f.getOriginalName())
                .fileUrl(f.getFileUrl()).fileSize(f.getFileSize())
                .fileType(f.getFileType()).uploadedAt(f.getUploadedAt()).build();
    }
}
