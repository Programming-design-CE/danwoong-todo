package com.danwoog.todo.domain.todogroup;

import com.danwoog.todo.domain.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "todo_groups")
public class TodoGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "group_name", length = 100, nullable = false)
    private String groupName;

    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "group_color")
    private String groupColor;

    @Column(name = "group_category", length = 100)
    private String groupCategory;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private GroupStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected TodoGroup() {
    }

    public TodoGroup(String groupName, String description, User createdBy) {
        this.groupName = groupName;
        this.description = description;
        this.createdBy = createdBy;
        this.status = GroupStatus.IN_PROGRESS;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public User getCreatedBy() {
        return createdBy;
    }
}