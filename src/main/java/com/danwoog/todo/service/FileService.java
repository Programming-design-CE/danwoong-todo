package com.danwoog.todo.service;

import com.danwoog.todo.domain.file.FileEntity;
import com.danwoog.todo.domain.file.Folder;
import com.danwoog.todo.domain.todogroup.TodoGroup;
import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.dto.file.FileDto.*;
import com.danwoog.todo.exception.CustomException.BadRequestException;
import com.danwoog.todo.exception.CustomException.BusinessException;
import com.danwoog.todo.exception.CustomException.ForbiddenException;
import com.danwoog.todo.exception.CustomException.NotFoundException;
import com.danwoog.todo.repository.MemberRepository;
import com.danwoog.todo.repository.TodoGroupRepository;
import com.danwoog.todo.repository.file.FileItemRepository;
import com.danwoog.todo.repository.file.FolderRepository;
import com.danwoog.todo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {

    private static final String LEGACY_ROOT_FOLDER_NAME = "기본 폴더";

    private final FolderRepository folderRepository;
    private final FileItemRepository fileItemRepository;
    private final UserRepository userRepository;
    private final TodoGroupRepository todoGroupRepository;
    private final MemberRepository memberRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Transactional
    public FolderResponse getRootFolder(Long groupId, Long userId) {
        TodoGroup group = getAuthorizedGroup(groupId, userId);
        User user = findUser(userId);
        return toFolderResponse(ensureRootFolder(group, user));
    }

    @Transactional
    public void initializeProjectFolder(TodoGroup group, User user) {
        ensureRootFolder(group, user);
    }

    @Transactional
    public void syncRootFolderName(TodoGroup group, String previousGroupName) {
        folderRepository.findByGroup_GroupIdAndParentFolderIdIsNull(group.getGroupId())
                .filter(root -> shouldSyncRootFolderName(root.getFolderName(), previousGroupName))
                .ifPresent(root -> root.rename(group.getGroupName()));
    }

    @Transactional
    public FolderResponse createSubFolder(Long groupId, Long parentFolderId,
                                          FolderCreateRequest request, Long userId) {
        TodoGroup group = getAuthorizedGroup(groupId, userId);
        User user = findUser(userId);
        validateFolderName(request.getFolderName());
        findGroupFolder(groupId, parentFolderId);

        Folder savedFolder = folderRepository.save(
                Folder.builder()
                        .group(group)
                        .folderName(request.getFolderName().trim())
                        .parentFolderId(parentFolderId)
                        .createdBy(user)
                        .build()
        );
        return toFolderResponse(savedFolder);
    }

    public FolderItemsResponse getFolderItems(Long groupId, Long folderId, Long userId) {
        getAuthorizedGroup(groupId, userId);
        Folder current = findGroupFolder(groupId, folderId);

        List<FolderResponse> subFolders = folderRepository
                .findByGroup_GroupIdAndParentFolderId(groupId, folderId)
                .stream()
                .map(this::toFolderResponse)
                .collect(Collectors.toList());

        List<FileResponse> files = fileItemRepository
                .findByGroup_GroupIdAndFolder(groupId, current)
                .stream()
                .map(this::toFileResponse)
                .collect(Collectors.toList());

        return FolderItemsResponse.builder()
                .currentFolder(toFolderResponse(current))
                .folders(subFolders)
                .files(files)
                .build();
    }

    @Transactional
    public FileResponse uploadFile(Long groupId, Long folderId,
                                   MultipartFile file, Long userId) throws IOException {
        TodoGroup group = getAuthorizedGroup(groupId, userId);
        User user = findUser(userId);
        Folder folder = findGroupFolder(groupId, folderId);
        validateUploadFile(file);

        String originalName = sanitizeOriginalFilename(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + "_" + originalName;
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Files.copy(
                file.getInputStream(),
                uploadPath.resolve(storedName),
                StandardCopyOption.REPLACE_EXISTING
        );

        FileEntity savedFile = fileItemRepository.save(
                new FileEntity(
                        group,
                        folder,
                        user,
                        originalName,
                        storedName,
                        null,
                        file.getSize(),
                        file.getContentType() != null ? file.getContentType() : "application/octet-stream"
                )
        );
        savedFile.updateFileUrl("/files/" + savedFile.getFileId());

        return toFileResponse(savedFile);
    }

    public FileEntity getFile(Long fileId, Long userId) {
        FileEntity file = fileItemRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("파일이 존재하지 않습니다."));
        validateGroupMember(file.getGroup().getGroupId(), userId);
        return file;
    }

    @Transactional
    public void deleteFile(Long fileId, Long userId) throws IOException {
        FileEntity file = getFile(fileId, userId);
        deleteStoredFile(file);
        fileItemRepository.delete(file);
    }

    @Transactional
    public void deleteFolder(Long folderId, Long userId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new NotFoundException("폴더가 존재하지 않습니다."));
        validateGroupMember(folder.getGroup().getGroupId(), userId);

        if (folder.getParentFolderId() == null) {
            throw new BadRequestException("루트 폴더는 삭제할 수 없습니다.");
        }

        try {
            deleteFolderTree(folder);
        } catch (IOException e) {
            throw new BusinessException("폴더 삭제 중 파일 정리에 실패했습니다.");
        }
    }

    @Transactional
    public void deleteProjectFiles(TodoGroup group) {
        try {
            deleteFilesWithoutFolder(group.getGroupId());

            folderRepository.findByGroup_GroupIdAndParentFolderIdIsNull(group.getGroupId())
                    .ifPresent(rootFolder -> {
                        try {
                            deleteFolderTree(rootFolder);
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    });

            List<Folder> remainingFolders = folderRepository.findByGroup_GroupId(group.getGroupId());
            for (Folder folder : remainingFolders) {
                deleteFolderTree(folder);
            }
        } catch (IllegalStateException e) {
            throw new BusinessException("?꾨줈?앺듃 ???뚯씪/폴더 ?뺣━???ㅽ뙣?덉뒿?덈떎.");
        } catch (IOException e) {
            throw new BusinessException("?꾨줈?앺듃 ???뚯씪/폴더 ?뺣━???ㅽ뙣?덉뒿?덈떎.");
        }
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }

    private TodoGroup findGroup(Long groupId) {
        return todoGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("그룹을 찾을 수 없습니다."));
    }

    private TodoGroup getAuthorizedGroup(Long groupId, Long userId) {
        TodoGroup group = findGroup(groupId);
        validateGroupMember(groupId, userId);
        return group;
    }

    private void validateGroupMember(Long groupId, Long userId) {
        if (!memberRepository.existsByGroup_GroupIdAndUser_UserId(groupId, userId)) {
            throw new ForbiddenException("해당 그룹의 멤버만 파일에 접근할 수 있습니다.");
        }
    }

    private Folder ensureRootFolder(TodoGroup group, User user) {
        return folderRepository.findByGroup_GroupIdAndParentFolderIdIsNull(group.getGroupId())
                .map(root -> {
                    if (shouldSyncRootFolderName(root.getFolderName(), group.getGroupName())) {
                        root.rename(group.getGroupName());
                    }
                    return root;
                })
                .orElseGet(() -> folderRepository.save(
                        Folder.builder()
                                .group(group)
                                .folderName(group.getGroupName())
                                .parentFolderId(null)
                                .createdBy(user)
                                .build()
                ));
    }

    private Folder findGroupFolder(Long groupId, Long folderId) {
        return folderRepository.findByIdAndGroup_GroupId(folderId, groupId)
                .orElseThrow(() -> new NotFoundException("해당 그룹의 폴더를 찾을 수 없습니다."));
    }

    private void validateFolderName(String folderName) {
        if (folderName == null || folderName.isBlank()) {
            throw new BadRequestException("폴더 이름을 입력해주세요.");
        }
    }

    private void validateUploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("업로드할 파일이 비어 있습니다.");
        }
    }

    private String sanitizeOriginalFilename(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "unnamed-file";
        }
        return Paths.get(originalName).getFileName().toString();
    }

    private void deleteFolderTree(Folder folder) throws IOException {
        List<FileEntity> files = fileItemRepository.findByGroup_GroupIdAndFolder(
                folder.getGroup().getGroupId(),
                folder
        );
        for (FileEntity file : files) {
            deleteStoredFile(file);
        }
        fileItemRepository.deleteAll(files);

        List<Folder> subFolders = folderRepository.findByGroup_GroupIdAndParentFolderId(
                folder.getGroup().getGroupId(),
                folder.getId()
        );
        for (Folder subFolder : subFolders) {
            deleteFolderTree(subFolder);
        }

        folderRepository.delete(folder);
    }

    private void deleteFilesWithoutFolder(Long groupId) throws IOException {
        List<FileEntity> filesWithoutFolder = fileItemRepository.findByGroup_GroupIdAndFolderIsNull(groupId);
        for (FileEntity file : filesWithoutFolder) {
            deleteStoredFile(file);
        }
        fileItemRepository.deleteAll(filesWithoutFolder);
    }

    private void deleteStoredFile(FileEntity file) throws IOException {
        Files.deleteIfExists(Paths.get(uploadDir, file.getStoredName()));
    }

    private boolean shouldSyncRootFolderName(String currentFolderName, String previousGroupName) {
        return currentFolderName == null
                || currentFolderName.isBlank()
                || LEGACY_ROOT_FOLDER_NAME.equals(currentFolderName)
                || currentFolderName.equals(previousGroupName);
    }

    private FolderResponse toFolderResponse(Folder folder) {
        return FolderResponse.builder()
                .folderId(folder.getId())
                .folderName(folder.getFolderName())
                .parentFolderId(folder.getParentFolderId())
                .createdAt(folder.getCreatedAt())
                .totalSize(calculateFolderSize(folder))
                .build();
    }

    private FileResponse toFileResponse(FileEntity file) {
        return FileResponse.builder()
                .fileId(file.getFileId())
                .originalName(file.getOriginalName())
                .fileUrl(file.getFileUrl() != null ? file.getFileUrl() : "/files/" + file.getFileId())
                .fileSize(file.getFileSize())
                .fileType(file.getFileType())
                .uploadedAt(file.getUploadedAt())
                .build();
    }

    private long calculateFolderSize(Folder folder) {
        long totalSize = fileItemRepository.findByGroup_GroupIdAndFolder(
                        folder.getGroup().getGroupId(),
                        folder
                )
                .stream()
                .map(FileEntity::getFileSize)
                .filter(size -> size != null && size > 0)
                .mapToLong(Long::longValue)
                .sum();

        List<Folder> subFolders = folderRepository.findByGroup_GroupIdAndParentFolderId(
                folder.getGroup().getGroupId(),
                folder.getId()
        );

        for (Folder subFolder : subFolders) {
            totalSize += calculateFolderSize(subFolder);
        }

        return totalSize;
    }
}
