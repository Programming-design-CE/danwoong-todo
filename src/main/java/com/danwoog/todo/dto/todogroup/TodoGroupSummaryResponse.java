package com.danwoog.todo.dto.todogroup;

import com.danwoog.todo.domain.todogroup.GroupStatus;
import com.danwoog.todo.domain.todogroup.Priority;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDate;
import java.util.List;

@JsonPropertyOrder({
        "group_id",
        "group_name",
        "deadline",
        "priority",
        "status",
        "members",
        "member_count"
})
public class TodoGroupSummaryResponse {

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

    @JsonProperty("members")
    private List<MemberPreviewResponse> members;

    @JsonProperty("member_count")
    private int memberCount;

    public TodoGroupSummaryResponse(
            Long groupId,
            String groupName,
            LocalDate deadline,
            Priority priority,
            GroupStatus status,
            List<MemberPreviewResponse> members,
            int memberCount
    ) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.deadline = deadline;
        this.priority = priority;
        this.status = status;
        this.members = members;
        this.memberCount = memberCount;
    }

    public Long getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public Priority getPriority() {
        return priority;
    }

    public GroupStatus getStatus() {
        return status;
    }

    public List<MemberPreviewResponse> getMembers() {
        return members;
    }

    public int getMemberCount() {
        return memberCount;
    }
}