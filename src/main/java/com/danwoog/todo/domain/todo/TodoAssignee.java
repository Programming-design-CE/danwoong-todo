package com.danwoog.todo.domain.todo;

import com.danwoog.todo.domain.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "todo_assignees")
public class TodoAssignee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignee_id")
    private Long assigneeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", nullable = false)
    private Todo todo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    protected TodoAssignee() {
    }

    public TodoAssignee(Todo todo, User user) {
        this.todo = todo;
        this.user = user;
        this.assignedAt = LocalDateTime.now();
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public Todo getTodo() {
        return todo;
    }

    public User getUser() {
        return user;
    }
}
