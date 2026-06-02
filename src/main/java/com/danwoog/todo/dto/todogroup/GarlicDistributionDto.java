package com.danwoog.todo.dto.todogroup;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GarlicDistributionDto {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("reward_amount")
    private Integer rewardAmount;

    public Long getUserId() {
        return userId;
    }

    public Integer getRewardAmount() {
        return rewardAmount;
    }
}
