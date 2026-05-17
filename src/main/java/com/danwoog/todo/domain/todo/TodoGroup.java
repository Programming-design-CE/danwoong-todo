package com.danwoog.todo.domain.todo;

import com.danwoog.todo.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "todo_groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodoGroup {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id;

    @Column(name = "group_name", length = 100)
    private String name;

    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "group_color", length = 255)
    private String groupColor;

    @Column(name = "group_category", length = 100)
    private String groupCategory;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "priority", length = 20)
    private String priority; // HIGH, MEDIUM, LOW

    @Column(name = "status", length = 20)
    private String status; // IN_PROGRESS, COMPLETED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public TodoGroup(String name) {
        this.name = name;
    }
}
