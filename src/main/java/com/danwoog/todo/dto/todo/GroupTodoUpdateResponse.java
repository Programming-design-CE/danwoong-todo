package com.danwoog.todo.dto.todo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GroupTodoUpdateResponse {

    @JsonProperty("todo_id")
    private final Long todoId;

    public GroupTodoUpdateResponse(Long todoId) {
        this.todoId = todoId;
    }

    public Long getTodoId() {
        return todoId;
    }
}
