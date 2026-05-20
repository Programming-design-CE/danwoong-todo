package com.danwoog.todo.dto.todo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GroupTodoAssignRequest {

    @JsonProperty("user_ids")
    private List<Long> userIds;

    public List<Long> getUserIds() {
        return userIds;
    }
}
