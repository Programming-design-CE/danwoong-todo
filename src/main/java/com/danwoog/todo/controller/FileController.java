package com.danwoog.todo.controller;

import com.danwoog.todo.common.ApiResponse;
import com.danwoog.todo.domain.file.FileEntity;
import com.danwoog.todo.dto.file.FileDto.FileResponse;
import com.danwoog.todo.dto.file.FileDto.FolderCreateRequest;
import com.danwoog.todo.dto.file.FileDto.FolderItemsResponse;
import com.danwoog.todo.dto.file.FileDto.FolderResponse;
import com.danwoog.todo.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @GetMapping("/todo-groups/{groupId}/folders/root")
    public ResponseEntity<ApiResponse<FolderResponse>> getRootFolder(
            Authentication authentication,
            @PathVariable("groupId") Long groupId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(fileService.getRootFolder(groupId, getLoginUserId(authentication)))
        );
    }

    @PostMapping("/todo-groups/{groupId}/folders/{folderId}/folders")
    public ResponseEntity<ApiResponse<FolderResponse>> createSubFolder(
            Authentication authentication,
            @PathVariable("groupId") Long groupId,
            @PathVariable("folderId") Long folderId,
            @RequestBody FolderCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        fileService.createSubFolder(groupId, folderId, request, getLoginUserId(authentication))
                ));
    }

    @GetMapping("/todo-groups/{groupId}/folders/{folderId}/items")
    public ResponseEntity<ApiResponse<FolderItemsResponse>> getFolderItems(
            Authentication authentication,
            @PathVariable("groupId") Long groupId,
            @PathVariable("folderId") Long folderId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(fileService.getFolderItems(groupId, folderId, getLoginUserId(authentication)))
        );
    }

    @PostMapping(
            value = "/todo-groups/{groupId}/folders/{folderId}/files",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(
            Authentication authentication,
            @PathVariable("groupId") Long groupId,
            @PathVariable("folderId") Long folderId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        fileService.uploadFile(groupId, folderId, file, getLoginUserId(authentication))
                ));
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<Resource> downloadFile(
            Authentication authentication,
            @PathVariable("fileId") Long fileId
    ) throws IOException {
        FileEntity fileItem = fileService.getFile(fileId, getLoginUserId(authentication));
        Resource resource = new UrlResource(Paths.get(uploadDir, fileItem.getStoredName()).toUri());

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(fileItem.getOriginalName(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .contentType(MediaType.parseMediaType(fileItem.getFileType()))
                .body(resource);
    }

    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            Authentication authentication,
            @PathVariable("fileId") Long fileId
    ) throws IOException {
        fileService.deleteFile(fileId, getLoginUserId(authentication));
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/folders/{folderId}")
    public ResponseEntity<ApiResponse<Void>> deleteFolder(
            Authentication authentication,
            @PathVariable("folderId") Long folderId
    ) {
        fileService.deleteFolder(folderId, getLoginUserId(authentication));
        return ResponseEntity.ok(ApiResponse.ok());
    }

    private Long getLoginUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
