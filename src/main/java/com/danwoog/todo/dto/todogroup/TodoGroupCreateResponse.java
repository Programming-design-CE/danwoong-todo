package com.danwoog.todo.dto.todogroup;

import com.danwoog.todo.domain.todogroup.GroupMemberRole;

public class TodoGroupCreateResponse {

    private Long groupId;
    private Long leaderId;
    private GroupMemberRole leaderRole;
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