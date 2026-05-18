package com.danwoog.todo.dto.todo;

import com.danwoog.todo.domain.todo.GarlicDistributionType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GroupTodoCreateResponse {

    @JsonProperty("todo_id")
    private final Long todoId;

    @JsonProperty("distribution_type")
    private final GarlicDistributionType distributionType;

    @JsonProperty("assignees")
    private final List<GroupTodoAssigneeResponse> assignees;

    public GroupTodoCreateResponse(
            Long todoId,
            GarlicDistributionType distributionType,
            List<GroupTodoAssigneeResponse> assignees
    ) {
        this.todoId = todoId;
        this.distributionType = distributionType;
        this.assignees = assignees;
    }

    public Long getTodoId() {
        return todoId;
    }

    public GarlicDistributionType getDistributionType() {
        return distributionType;
    }

    public List<GroupTodoAssigneeResponse> getAssignees() {
        return assignees;
    }
}
