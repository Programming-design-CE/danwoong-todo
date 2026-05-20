package com.danwoog.todo.dto.todo;

import com.danwoog.todo.domain.todo.GarlicDistributionType;
import com.danwoog.todo.domain.todo.TodoStatus;
import com.danwoog.todo.domain.todogroup.Priority;

import java.time.LocalDateTime;

public class GroupTodoDetailView {

    private final Long todoId;
    private final String todoName;
    private final String description;
    private final LocalDateTime deadline;
    private final Priority priority;
    private final TodoStatus status;
    private final Integer garlicReward;
    private final String category;
    private final GarlicDistributionType distributionType;

    public GroupTodoDetailView(
            Long todoId,
            String todoName,
            String description,
            LocalDateTime deadline,
            Priority priority,
            TodoStatus status,
            Integer garlicReward,
            String category,
            GarlicDistributionType distributionType
    ) {
        this.todoId = todoId;
        this.todoName = todoName;
        this.description = description;
        this.deadline = deadline;
        this.priority = priority;
        this.status = status;
        this.garlicReward = garlicReward;
        this.category = category;
        this.distributionType = distributionType;
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

    public LocalDateTime getDeadline() {
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

    public GarlicDistributionType getDistributionType() {
        return distributionType;
    }
}
