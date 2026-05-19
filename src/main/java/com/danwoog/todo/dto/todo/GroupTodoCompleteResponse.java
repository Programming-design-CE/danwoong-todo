package com.danwoog.todo.dto.todo;

import com.danwoog.todo.domain.todo.TodoStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GroupTodoCompleteResponse {

    @JsonProperty("todo_id")
    private final Long todoId;

    @JsonProperty("status")
    private final TodoStatus status;

    @JsonProperty("garlic_reward")
    private final Integer garlicReward;

    @JsonProperty("rewarded_assignees")
    private final List<GroupTodoAssigneeResponse> rewardedAssignees;

    public GroupTodoCompleteResponse(
            Long todoId,
            TodoStatus status,
            Integer garlicReward,
            List<GroupTodoAssigneeResponse> rewardedAssignees
    ) {
        this.todoId = todoId;
        this.status = status;
        this.garlicReward = garlicReward;
        this.rewardedAssignees = rewardedAssignees;
    }

    public Long getTodoId() {
        return todoId;
    }

    public TodoStatus getStatus() {
        return status;
    }

    public Integer getGarlicReward() {
        return garlicReward;
    }

    public List<GroupTodoAssigneeResponse> getRewardedAssignees() {
        return rewardedAssignees;
    }
}
