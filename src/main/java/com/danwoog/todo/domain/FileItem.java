package com.danwoog.todo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "files")
public class FileItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private TodoGroup group;

    // null이면 루트에 직접 저장
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(nullable = false, length = 255)
    private String originalName;

    @Column(nullable = false, length = 255)
    private String storedName;

    @Column(nullable = false, length = 255)
    private String fileUrl;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 50)
    private String fileType;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Builder
    public FileItem(TodoGroup group, Folder folder, User uploadedBy,
                    String originalName, String storedName, String fileUrl,
                    Long fileSize, String fileType) {
        this.group = group;
        this.folder = folder;
        this.uploadedBy = uploadedBy;
        this.originalName = originalName;
        this.storedName = storedName;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.fileType = fileType;
    }

    @PrePersist
    protected void onCreate() { uploadedAt = LocalDateTime.now(); }
}
