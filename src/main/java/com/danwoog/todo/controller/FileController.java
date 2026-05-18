package com.danwoog.todo.controller;

import com.danwoog.todo.common.ApiResponse;
import com.danwoog.todo.dto.file.FileDto.*;
import com.danwoog.todo.domain.file.FileEntity;
import com.danwoog.todo.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    private final Long TEMP_MEMBER_ID = 1L;

    // FileService와 동일한 경로 — application.properties 변경 시 자동 반영
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /** GET /todo-groups/{groupId}/folders/root — 루트 폴더 조회 */
    @GetMapping("/todo-groups/{groupId}/folders/root")
    public ResponseEntity<ApiResponse<FolderResponse>> getRootFolder(
            @PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.ok(fileService.getRootFolder(groupId, TEMP_MEMBER_ID)));
    }

    /** POST /todo-groups/{groupId}/folders/{folderId}/folders — 하위 폴더 생성 */
    @PostMapping("/todo-groups/{groupId}/folders/{folderId}/folders")
    public ResponseEntity<ApiResponse<FolderResponse>> createSubFolder(
            @PathVariable Long groupId,
            @PathVariable Long folderId,
            @RequestBody FolderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(fileService.createSubFolder(groupId, folderId, request, TEMP_MEMBER_ID)));
    }

    /** GET /todo-groups/{groupId}/folders/{folderId}/items — 폴더 내부 항목 조회 */
    @GetMapping("/todo-groups/{groupId}/folders/{folderId}/items")
    public ResponseEntity<ApiResponse<FolderItemsResponse>> getFolderItems(
            @PathVariable Long groupId,
            @PathVariable Long folderId) {
        return ResponseEntity.ok(ApiResponse.ok(fileService.getFolderItems(groupId, folderId)));
    }

    /** POST /todo-groups/{groupId}/folders/{folderId}/files — 파일 업로드 */
    @PostMapping(value = "/todo-groups/{groupId}/folders/{folderId}/files", 
                consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(
            @PathVariable Long groupId,
            @PathVariable Long folderId,
            @RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(fileService.uploadFile(groupId, folderId, file, TEMP_MEMBER_ID)));
    }

    /** GET /files/{fileId} — 파일 다운로드 */
    @GetMapping("/files/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) throws IOException {
        FileEntity fileItem = fileService.getFile(fileId);
        Resource resource = new UrlResource(Paths.get(uploadDir, fileItem.getStoredName()).toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileItem.getOriginalName() + "\"")
                .contentType(MediaType.parseMediaType(fileItem.getFileType()))
                .body(resource);
    }

    /** DELETE /files/{fileId} — 파일 삭제 */
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Long fileId) throws IOException {
        fileService.deleteFile(fileId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    /** DELETE /folders/{folderId} — 폴더 삭제 */
    @DeleteMapping("/folders/{folderId}")
    public ResponseEntity<ApiResponse<Void>> deleteFolder(@PathVariable Long folderId) {
        fileService.deleteFolder(folderId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
