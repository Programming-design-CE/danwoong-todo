package com.danwoog.todo.dto.todo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public class GroupTodoUpdateRequest {

    @JsonProperty("todo_name")
    private String todoName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("deadline")
    private LocalDate deadline;

    @JsonProperty("priority")
    private String priority;

    @JsonProperty("category")
    private String category;

    @JsonProperty("status")
    private String status;

    @JsonProperty("garlic_reward")
    private Integer garlicReward;

    @JsonProperty("distribution_type")
    private String distributionType;

    @JsonProperty("assignees")
    private List<GroupTodoAssigneeRequest> assignees;

    public String getTodoName() {
        return todoName;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public String getPriority() {
        return priority;
    }

    public String getCategory() {
        return category;
    }

    public String getStatus() {
        return status;
    }

    public Integer getGarlicReward() {
        return garlicReward;
    }

    public String getDistributionType() {
        return distributionType;
    }

    public List<GroupTodoAssigneeRequest> getAssignees() {
        return assignees;
    }
}
