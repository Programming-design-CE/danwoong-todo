package com.danwoog.todo.dto.todogroup;

import com.danwoog.todo.domain.todogroup.GroupStatus;
import com.danwoog.todo.domain.todogroup.Priority;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public class TodoGroupCreateResponse {

    @JsonProperty("group_id")
    private Long groupId;

    @JsonProperty("group_name")
    private String groupName;

    private LocalDate deadline;

    private Priority priority;

    private GroupStatus status;

    @JsonProperty("total_garlic_reward")
    private Integer totalGarlicReward;

    @JsonProperty("remaining_garlic_reward")
    private Integer remainingGarlicReward;

    @JsonProperty("members")
    private List<MemberPreviewResponse> members;

    @JsonProperty("member_count")
    private int memberCount;

    public TodoGroupCreateResponse(
            Long groupId,
            String groupName,
            LocalDate deadline,
            Priority priority,
            GroupStatus status,
            Integer totalGarlicReward,
            Integer remainingGarlicReward,
            List<MemberPreviewResponse> members,
            int memberCount
    ) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.deadline = deadline;
        this.priority = priority;
        this.status = status;
        this.totalGarlicReward = totalGarlicReward;
        this.remainingGarlicReward = remainingGarlicReward;
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

    public Integer getTotalGarlicReward() {
        return totalGarlicReward;
    }

    public Integer getRemainingGarlicReward() {
        return remainingGarlicReward;
    }

    public List<MemberPreviewResponse> getMembers() {
        return members;
    }

    public int getMemberCount() {
        return memberCount;
    }
}
