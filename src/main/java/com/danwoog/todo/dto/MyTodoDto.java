package com.danwoog.todo.dto;

import com.danwoog.todo.domain.todo.TodoAssignee;
import lombok.Getter;

@Getter
public class MyTodoDto {
    private Long todo_id;
    private String todo_name;
    private String group_name;
    private String deadline;
    
    public MyTodoDto(TodoAssignee ta) {
        this.todo_id = ta.getTodo().getId();
        this.todo_name = ta.getTodo().getTitle();
        this.group_name = ta.getTodo().getGroup() != null ? ta.getTodo().getGroup().getName() : null;
        this.deadline = ta.getTodo().getDeadline() != null ? ta.getTodo().getDeadline().toString() : null;
    }
}
