package com.danwoog.todo.dto.todogroup;

import com.danwoog.todo.domain.todogroup.GroupMemberRole;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TodoGroupCreateResponse {

    @JsonProperty("group_id")
    private Long groupId;

    @JsonProperty("leader_id")
    private Long leaderId;

    @JsonProperty("leader_role")
    private GroupMemberRole leaderRole;

    @JsonProperty("invitation_count")
    private int invitationCount;

    public TodoGroupCreateResponse(Long groupId, Long leaderId, GroupMemberRole leaderRole, int invitationCount) {
        this.groupId = groupId;
        this.leaderId = leaderId;
        this.leaderRole = leaderRole;
        this.invitationCount = invitationCount;
    }

    public Long getGroupId() {
        return groupId;
    }

    public Long getLeaderId() {
        return leaderId;
    }

    public GroupMemberRole getLeaderRole() {
        return leaderRole;
    }

    public int getInvitationCount() {
        return invitationCount;
    }
}