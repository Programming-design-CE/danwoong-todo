package com.danwoog.todo.dto.file;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public class FileDto {

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class FolderCreateRequest {
        private String folderName;
    }

    @Getter @Builder
    public static class FolderResponse {
        private Long folderId;
        private String folderName;
        private Long parentFolderId;
        private LocalDateTime createdAt;
        private Long totalSize;
    }

    @Getter @Builder
    public static class FileResponse {
        private Long fileId;
        private String originalName;
        private String fileUrl;
        private Long fileSize;
        private String fileType;
        private LocalDateTime uploadedAt;
    }

    @Getter @Builder
    public static class FolderItemsResponse {
        private FolderResponse currentFolder;
        private List<FolderResponse> folders;
        private List<FileResponse> files;
    }
}
