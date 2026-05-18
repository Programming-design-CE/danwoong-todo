package com.danwoog.todo.dto.todogroup;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TodoGroupInviteRequest {

    @JsonProperty("member_ids")
    private List<Long> memberIds;

    public List<Long> getMemberIds() {
        return memberIds;
    }
}