package com.danwoog.todo.dto.todogroup;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;

public class TodoGroupCreateRequest {


    // JSON에서는 "group_name"으로 받고, Java에서는 groupName 변수에 넣어주는 어노테이션
    @JsonProperty("group_name")
    private String groupName;

    @JsonProperty("group_icon_url")
    private String groupIconUrl;

    @JsonProperty("group_category")
    private String groupCategory;

    @JsonProperty("deadline")
    private LocalDate deadline;

    @JsonProperty("priority")
    private String priority;

    @JsonProperty("invitee_ids")
    private List<Long> inviteeIds;

    @JsonProperty("total_garlic_reward")
    private Integer totalGarlicReward;

    public String getGroupName() {
        return groupName;
    }

    public String getGroupIconUrl() {
        return groupIconUrl;
    }

    public String getGroupCategory() {
        return groupCategory;
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

    public Integer getTotalGarlicReward() {
        return totalGarlicReward;
    }
}
