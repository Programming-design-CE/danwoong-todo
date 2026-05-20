package com.danwoog.todo.dto.todo;

import com.danwoog.todo.domain.todo.TodoAssignee;
import lombok.Getter;

@Getter
public class MyTodoDto {
    private Long todo_id;
    private String todo_name;
    private String group_name;
    private String deadline;
    
    public MyTodoDto(TodoAssignee ta) {
        this.todo_id = ta.getTodo().getTodoId();
        this.todo_name = ta.getTodo().getTodoName();
        this.group_name = ta.getTodo().getGroup() != null ? ta.getTodo().getGroup().getGroupName() : null;
        this.deadline = ta.getTodo().getDeadline() != null ? ta.getTodo().getDeadline().toString() : null;
    }
}
