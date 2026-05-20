package com.danwoog.todo.dto.todogroup;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TodoGroupInviteResponse {

    @JsonProperty("group_id")
    private Long groupId;

    @JsonProperty("invited_member_count")
    private int invitedMemberCount;

    public TodoGroupInviteResponse(Long groupId, int invitedMemberCount) {
        this.groupId = groupId;
        this.invitedMemberCount = invitedMemberCount;
    }

    public Long getGroupId() {
        return groupId;
    }

    public int getInvitedMemberCount() {
        return invitedMemberCount;
    }
}