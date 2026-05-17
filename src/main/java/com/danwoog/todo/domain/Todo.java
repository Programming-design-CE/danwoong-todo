package com.danwoog.todo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Todo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private TodoGroup group;
    
    private LocalDate deadline;
    private String category;
    private Integer garlicReward;
    
    @Enumerated(EnumType.STRING)
    private TodoPriority priority;
    
    private String description;
    
    @Builder
    public Todo(String title, TodoGroup group, LocalDate deadline, String category, Integer garlicReward, TodoPriority priority, String description) {
        this.title = title;
        this.group = group;
        this.deadline = deadline;
        this.category = category;
        this.garlicReward = garlicReward;
        this.priority = priority;
        this.description = description;
    }
}
