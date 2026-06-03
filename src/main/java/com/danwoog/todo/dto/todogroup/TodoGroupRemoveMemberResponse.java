package com.danwoog.todo.dto.todogroup;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TodoGroupRemoveMemberResponse {

    @JsonProperty("group_id")
    private Long groupId;

    @JsonProperty("removed_member_count")
    private int removedMemberCount;

    public TodoGroupRemoveMemberResponse(Long groupId, int removedMemberCount) {
        this.groupId = groupId;
        this.removedMemberCount = removedMemberCount;
    }

    public Long getGroupId() {
        return groupId;
    }

    public int getRemovedMemberCount() {
        return removedMemberCount;
    }
}
