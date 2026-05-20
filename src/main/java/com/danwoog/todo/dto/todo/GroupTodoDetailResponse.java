package com.danwoog.todo.dto.todo;

import com.danwoog.todo.domain.todo.GarlicDistributionType;
import com.danwoog.todo.domain.todo.TodoCategory;
import com.danwoog.todo.domain.todo.TodoStatus;
import com.danwoog.todo.domain.todogroup.Priority;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public class GroupTodoDetailResponse {

    @JsonProperty("todo_id")
    private final Long todoId;

    @JsonProperty("todo_name")
    private final String todoName;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("deadline")
    private final LocalDate deadline;

    @JsonProperty("priority")
    private final Priority priority;

    @JsonProperty("status")
    private final TodoStatus status;

    @JsonProperty("garlic_reward")
    private final Integer garlicReward;

    @JsonProperty("category")
    private final String category;

    @JsonProperty("distribution_type")
    private final GarlicDistributionType distributionType;

    @JsonProperty("assignees")
    private final List<GroupTodoAssigneeResponse> assignees;

    public GroupTodoDetailResponse(GroupTodoDetailView todo, List<GroupTodoAssigneeResponse> assignees) {
        this.todoId = todo.getTodoId();
        this.todoName = todo.getTodoName();
        this.description = todo.getDescription();
        this.deadline = todo.getDeadline() != null ? todo.getDeadline().toLocalDate() : null;
        this.priority = todo.getPriority();
        this.status = todo.getStatus();
        this.garlicReward = todo.getGarlicReward();
        this.category = toCategoryLabel(todo.getCategory());
        this.distributionType = todo.getDistributionType();
        this.assignees = assignees;
    }

    public Long getTodoId() {
        return todoId;
    }

    public String getTodoName() {
        return todoName;
    }

    public String getDescription() {
        return description;
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

    public Integer getGarlicReward() {
        return garlicReward;
    }

    public String getCategory() {
        return category;
    }

    private String toCategoryLabel(TodoCategory category) {
        return category != null ? category.getLabel() : null;
    }

    public GarlicDistributionType getDistributionType() {
        return distributionType;
    }

    public List<GroupTodoAssigneeResponse> getAssignees() {
        return assignees;
    }
}
