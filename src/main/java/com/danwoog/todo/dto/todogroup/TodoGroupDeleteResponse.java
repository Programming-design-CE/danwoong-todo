package com.danwoog.todo.dto.todogroup;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TodoGroupDeleteResponse {

    @JsonProperty("group_id")
    private Long groupId;

    @JsonProperty("status")
    private String status;

    public TodoGroupDeleteResponse(Long groupId, String status) {
        this.groupId = groupId;
        this.status = status;
    }

    public Long getGroupId() {
        return groupId;
    }

    public String getStatus() {
        return status;
    }
}