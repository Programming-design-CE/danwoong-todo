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
        "group_color",
        "group_category",
        "deadline",
        "priority",
        "status",
        "total_garlic_reward",
        "remaining_garlic_reward",
        "members",
        "member_count",
        "total_todo_count",
        "completed_todo_count",
        "leader_id",
        "updated_at"
})
public class TodoGroupSummaryResponse {

    @JsonProperty("group_id")
    private Long groupId;

    @JsonProperty("group_name")
    private String groupName;

    @JsonProperty("group_color")
    private String groupColor;

    @JsonProperty("group_category")
    private String groupCategory;

    @JsonProperty("deadline")
    private LocalDate deadline;

    @JsonProperty("priority")
    private Priority priority;

    @JsonProperty("status")
    private GroupStatus status;

    @JsonProperty("total_garlic_reward")
    private Integer totalGarlicReward;

    @JsonProperty("remaining_garlic_reward")
    private Integer remainingGarlicReward;

    @JsonProperty("members")
    private List<MemberPreviewResponse> members;

    @JsonProperty("member_count")
    private int memberCount;

    @JsonProperty("total_todo_count")
    private int totalTodoCount;

    @JsonProperty("completed_todo_count")
    private int completedTodoCount;

    @JsonProperty("leader_id")
    private Long leaderId;

    @JsonProperty("updated_at")
    private LocalDate updatedAt;

    public TodoGroupSummaryResponse(
            Long groupId,
            String groupName,
            String groupColor,
            String groupCategory,
            LocalDate deadline,
            Priority priority,
            GroupStatus status,
            Integer totalGarlicReward,
            Integer remainingGarlicReward,
            List<MemberPreviewResponse> members,
            int memberCount,
            int totalTodoCount,
            int completedTodoCount,
            Long leaderId,
            LocalDate updatedAt
    ) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupColor = groupColor;
        this.groupCategory = groupCategory;
        this.deadline = deadline;
        this.priority = priority;
        this.status = status;
        this.totalGarlicReward = totalGarlicReward;
        this.remainingGarlicReward = remainingGarlicReward;
        this.members = members;
        this.memberCount = memberCount;
        this.totalTodoCount = totalTodoCount;
        this.completedTodoCount = completedTodoCount;
        this.leaderId = leaderId;
        this.updatedAt = updatedAt;
    }

    public Long getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupColor() {
        return groupColor;
    }

    public String getGroupCategory() {
        return groupCategory;
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

    public int getTotalTodoCount() {
        return totalTodoCount;
    }

    public int getCompletedTodoCount() {
        return completedTodoCount;
    }

    public Long getLeaderId() {
        return leaderId;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }
}
