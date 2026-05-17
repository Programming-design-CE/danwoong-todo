package com.danwoog.todo.domain.todo;

import com.danwoog.todo.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "todos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Todo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private TodoGroup group;

    @Column(name = "todo_name", length = 100)
    private String todoName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "garlic_reward")
    private Integer garlicReward;

    @Column(name = "priority", length = 20)
    private String priority; // HIGH, MEDIUM, LOW

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "status", length = 20)
    private String status; // IN_PROGRESS, COMPLETED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by")
    private User completedBy;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

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

    @Builder
    public Todo(TodoGroup group, String todoName, String description, LocalDateTime deadline, Integer garlicReward, String priority, String category) {
        this.group = group;
        this.todoName = todoName;
        this.description = description;
        this.deadline = deadline;
        this.garlicReward = garlicReward;
        this.priority = priority;
        this.category = category;
        this.status = TodoStatus.IN_PROGRESS.name();
    }
}
