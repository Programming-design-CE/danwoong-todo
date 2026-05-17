package com.danwoog.todo.domain.todo;

import com.danwoog.todo.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "todo_assignees")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodoAssignee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignee_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id")
    private Todo todo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "assigned_at", updatable = false)
    private LocalDateTime assignedAt;

    @PrePersist
    protected void onCreate() {
        this.assignedAt = LocalDateTime.now();
    }

    @Builder
    public TodoAssignee(Todo todo, User user) {
        this.todo = todo;
        this.user = user;
    }
}
