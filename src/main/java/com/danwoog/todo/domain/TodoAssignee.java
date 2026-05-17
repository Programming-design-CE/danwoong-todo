package com.danwoog.todo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodoAssignee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id")
    private Todo todo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    
    @Enumerated(EnumType.STRING)
    private TodoStatus status = TodoStatus.IN_PROGRESS;
    
    private LocalDateTime completedAt;
    
    @Builder
    public TodoAssignee(Todo todo, Member member) {
        this.todo = todo;
        this.member = member;
        this.status = TodoStatus.IN_PROGRESS;
    }
    
    public void complete() {
        this.status = TodoStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}
