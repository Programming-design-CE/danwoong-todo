package com.danwoog.todo.service;

import com.danwoog.todo.domain.*;
import com.danwoog.todo.dto.file.FileDto.*;
import com.danwoog.todo.exception.CustomException.*;
import com.danwoog.todo.repository.*;
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
        Folder root = folderRepository.findByGroup_IdAndParentFolderIdIsNull(groupId)
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
                .findByGroup_IdAndParentFolderId(groupId, folderId).stream()
                .map(this::toFolderResponse).collect(Collectors.toList());
        List<FileResponse> files = fileItemRepository
                .findByGroup_IdAndFolder(groupId, current).stream()
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

        return toFileResponse(fileItemRepository.save(FileItem.builder()
                .group(group).folder(folder).uploadedBy(user)
                .originalName(originalName).storedName(storedName)
                .fileUrl("/files/" + storedName).fileSize(file.getSize())
                .fileType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .build()));
    }

    public FileItem getFile(Long fileId) {
        return fileItemRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("파일이 존재하지 않습니다."));
    }

    @Transactional
    public void deleteFile(Long fileId) throws IOException {
        FileItem file = getFile(fileId);
        Files.deleteIfExists(Paths.get(uploadDir, file.getStoredName()));
        fileItemRepository.delete(file);
    }

    @Transactional
    public void deleteFolder(Long folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new NotFoundException("폴더가 존재하지 않습니다."));
        fileItemRepository.deleteAll(
                fileItemRepository.findByGroup_IdAndFolder(folder.getGroup().getId(), folder));
        folderRepository.findByGroup_IdAndParentFolderId(folder.getGroup().getId(), folderId)
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

    private FileResponse toFileResponse(FileItem f) {
        return FileResponse.builder().fileId(f.getId()).originalName(f.getOriginalName())
                .fileUrl(f.getFileUrl()).fileSize(f.getFileSize())
                .fileType(f.getFileType()).uploadedAt(f.getUploadedAt()).build();
    }
}
