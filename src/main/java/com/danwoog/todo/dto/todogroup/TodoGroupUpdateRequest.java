package com.danwoog.todo.dto.todogroup;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public class TodoGroupUpdateRequest {

    @JsonProperty("group_id")
    private Long groupId;

    @JsonProperty("group_name")
    private String groupName;

    @JsonProperty("deadline")
    private LocalDate deadline;

    @JsonProperty("priority")
    private String priority;

    @JsonProperty("status")
    private String status;

    public Long getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }
    public LocalDate getDeadline() { return deadline; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
}