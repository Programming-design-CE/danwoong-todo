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

    public TodoGroupUpdateResponse(
            Long groupId,
            String groupName,
            String groupColor,
            String groupCategory,
            LocalDate deadline,
            Priority priority,
            GroupStatus status,
            Integer totalGarlicReward,
            Integer remainingGarlicReward
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
    }

    public Long getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }
    public String getGroupColor() { return groupColor; }
    public String getGroupCategory() { return groupCategory; }
    public LocalDate getDeadline() { return deadline; }
    public Priority getPriority() { return priority; }
    public GroupStatus getStatus() { return status; }
    public Integer getTotalGarlicReward() { return totalGarlicReward; }
    public Integer getRemainingGarlicReward() { return remainingGarlicReward; }
}
