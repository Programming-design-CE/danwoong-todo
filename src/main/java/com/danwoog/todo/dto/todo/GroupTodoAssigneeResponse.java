package com.danwoog.todo.dto.todo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GroupTodoAssigneeResponse {

    @JsonProperty("user_id")
    private final Long userId;

    @JsonProperty("nickname")
    private final String nickname;

    @JsonProperty("reward_amount")
    private final Integer rewardAmount;

    public GroupTodoAssigneeResponse(Long userId, String nickname, Integer rewardAmount) {
        this.userId = userId;
        this.nickname = nickname;
        this.rewardAmount = rewardAmount;
    }

    public Long getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public Integer getRewardAmount() {
        return rewardAmount;
    }
}
