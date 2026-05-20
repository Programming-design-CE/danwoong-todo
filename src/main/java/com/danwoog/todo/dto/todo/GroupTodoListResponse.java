package com.danwoog.todo.dto.todo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GroupTodoListResponse {

    @JsonProperty("todos")
    private final List<GroupTodoSummaryDto> todos;

    public GroupTodoListResponse(List<GroupTodoSummaryDto> todos) {
        this.todos = todos;
    }

    public List<GroupTodoSummaryDto> getTodos() {
        return todos;
    }
}
