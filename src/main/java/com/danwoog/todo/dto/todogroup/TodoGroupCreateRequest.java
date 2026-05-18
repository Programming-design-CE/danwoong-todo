package com.danwoog.todo.dto.todogroup;

import java.util.List;

public class TodoGroupCreateRequest {

    private String groupName;
    private List<Long> inviteeIds;

    public String getGroupName() {
        return groupName;
    }

    public List<Long> getInviteeIds() {
        return inviteeIds;
    }
}