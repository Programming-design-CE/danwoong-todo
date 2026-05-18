package com.danwoog.todo.dto.todogroup;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;

public class TodoGroupCreateRequest {

    @JsonProperty("group_name")
    private String groupName;

    @JsonProperty("group_icon_url")
    private String groupIconUrl;

    @JsonProperty("deadline")
    private LocalDate deadline;

    @JsonProperty("priority")
    private String priority;

    @JsonProperty("invitee_ids")
    private List<Long> inviteeIds;

    public String getGroupName() {
        return groupName;
    }

    public String getGroupIconUrl() {
        return groupIconUrl;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public String getPriority() {
        return priority;
    }

    public List<Long> getInviteeIds() {
        return inviteeIds;
    }
}