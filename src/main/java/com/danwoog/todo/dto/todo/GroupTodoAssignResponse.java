package com.danwoog.todo.dto.todo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GroupTodoAssignResponse {

    @JsonProperty("todo_id")
    private final Long todoId;

    @JsonProperty("assignees")
    private final List<GroupTodoAssigneeResponse> assignees;

    public GroupTodoAssignResponse(Long todoId, List<GroupTodoAssigneeResponse> assignees) {
        this.todoId = todoId;
        this.assignees = assignees;
    }

    public Long getTodoId() {
        return todoId;
    }

    public List<GroupTodoAssigneeResponse> getAssignees() {
        return assignees;
    }
}
