package com.danwoog.todo.domain.todo;

import com.danwoog.todo.domain.todogroup.Priority;
import com.danwoog.todo.domain.todogroup.TodoGroup;
import com.danwoog.todo.domain.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "todos")
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id")
    private Long todoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private TodoGroup group;

    @Column(name = "todo_name", length = 100, nullable = false)
    private String todoName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "garlic_reward")
    private Integer garlicReward;

    @Enumerated(EnumType.STRING)
    @Column(name = "distribution_type", length = 20)
    private GarlicDistributionType distributionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private Priority priority;

    @Column(name = "category", length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private TodoStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by")
    private User completedBy;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Todo() {
    }

    public Todo(TodoGroup group, String todoName, String description, User createdBy) {
        this.group = group;
        this.todoName = todoName;
        this.description = description;
        this.createdBy = createdBy;
        this.distributionType = GarlicDistributionType.EVEN;
        this.status = TodoStatus.IN_PROGRESS;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getTodoId() {
        return todoId;
    }

    public TodoGroup getGroup() {
        return group;
    }

    public String getTodoName() {
        return todoName;
    }

    public TodoStatus getStatus() {
        return status;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public Integer getGarlicReward() {
        return garlicReward;
    }

    public void setGarlicReward(Integer garlicReward) {
        this.garlicReward = garlicReward;
    }

    public GarlicDistributionType getDistributionType() {
        return distributionType;
    }

    public void setDistributionType(GarlicDistributionType distributionType) {
        this.distributionType = distributionType;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void complete(User completedBy) {
        this.status = TodoStatus.COMPLETED;
        this.completedBy = completedBy;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
