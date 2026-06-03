package com.danwoog.todo.domain.file;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.danwoog.todo.domain.todogroup.TodoGroup;
import com.danwoog.todo.domain.user.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "folders")
public class Folder {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private TodoGroup group;

    @Column(nullable = false, length = 100)
    private String folderName;

    // null이면 루트 폴더
    @Column(nullable = true)
    private Long parentFolderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Folder(TodoGroup group, String folderName, Long parentFolderId, User createdBy) {
        this.group = group;
        this.folderName = folderName;
        this.parentFolderId = parentFolderId;
        this.createdBy = createdBy;
    }

    public void rename(String folderName) {
        this.folderName = folderName;
    }

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
