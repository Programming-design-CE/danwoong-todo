package com.danwoog.todo.domain.file;

import com.danwoog.todo.domain.todogroup.TodoGroup;
import com.danwoog.todo.domain.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "files")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private TodoGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "stored_name")
    private String storedName;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    protected FileEntity() {
    }

    public FileEntity(TodoGroup group, Folder folder, User uploadedBy,
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
        this.uploadedAt = LocalDateTime.now();
    }

    public Long getFileId() {
        return fileId;
    }
}