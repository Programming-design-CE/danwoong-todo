package com.danwoog.todo.dto;

import com.danwoog.todo.domain.todo.TodoAssignee;
import lombok.Getter;
import java.time.format.DateTimeFormatter;

@Getter
public class MyCompletedTodoDto {
    private Long todo_id;
    private String todo_name;
    private String completed_at;
    private Integer garlic_reward;
    
    public MyCompletedTodoDto(TodoAssignee ta) {
        this.todo_id = ta.getTodo().getId();
        this.todo_name = ta.getTodo().getTodoName();
        this.completed_at = ta.getTodo().getCompletedAt() != null ? ta.getTodo().getCompletedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
        this.garlic_reward = ta.getTodo().getGarlicReward();
    }
}
