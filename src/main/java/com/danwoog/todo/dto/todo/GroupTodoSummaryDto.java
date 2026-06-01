package com.danwoog.todo.dto.todo;

import com.danwoog.todo.domain.todo.Todo;
import com.danwoog.todo.domain.todo.GarlicDistributionType;
import com.danwoog.todo.domain.todo.TodoStatus;
import com.danwoog.todo.domain.todogroup.Priority;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public class GroupTodoSummaryDto {

    @JsonProperty("todo_id")
    private final Long todoId;

    @JsonProperty("todo_name")
    private final String todoName;

    @JsonProperty("deadline")
    private final LocalDate deadline;

    @JsonProperty("priority")
    private final Priority priority;

    @JsonProperty("status")
    private final TodoStatus status;

    @JsonProperty("distribution_type")
    private final GarlicDistributionType distributionType;

    @JsonProperty("garlic_reward")
    private final Integer garlicReward;

    @JsonProperty("assignees")
    private final List<GroupTodoAssigneeResponse> assignees;

    public GroupTodoSummaryDto(Todo todo, List<GroupTodoAssigneeResponse> assignees) {
        this.todoId = todo.getTodoId();
        this.todoName = todo.getTodoName();
        this.deadline = todo.getDeadline() != null ? todo.getDeadline().toLocalDate() : null;
        this.priority = todo.getPriority();
        this.status = todo.getStatus();
        this.distributionType = todo.getDistributionType();
        this.garlicReward = todo.getGarlicReward();
        this.assignees = assignees;
    }

    public Long getTodoId() {
        return todoId;
    }

    public String getTodoName() {
        return todoName;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public Priority getPriority() {
        return priority;
    }

    public TodoStatus getStatus() {
        return status;
    }

    public GarlicDistributionType getDistributionType() {
        return distributionType;
    }

    public Integer getGarlicReward() {
        return garlicReward;
    }

    public List<GroupTodoAssigneeResponse> getAssignees() {
        return assignees;
    }
}
