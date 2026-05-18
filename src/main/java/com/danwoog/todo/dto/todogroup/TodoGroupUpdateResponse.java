package com.danwoog.todo.dto.todogroup;

import com.danwoog.todo.domain.todogroup.GroupStatus;
import com.danwoog.todo.domain.todogroup.Priority;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public class TodoGroupUpdateResponse {

    @JsonProperty("group_id")
    private Long groupId;

    @JsonProperty("group_name")
    private String groupName;

    @JsonProperty("deadline")
    private LocalDate deadline;

    @JsonProperty("priority")
    private Priority priority;

    @JsonProperty("status")
    private GroupStatus status;

    public TodoGroupUpdateResponse(Long groupId, String groupName, LocalDate deadline,  Priority priority, GroupStatus status) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.deadline = deadline;
        this.priority = priority;
        this.status = status;
    }

    public Long getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }
    public LocalDate getDeadline() { return deadline; }
    public Priority getPriority() { return priority; }
    public GroupStatus getStatus() { return status; }
}