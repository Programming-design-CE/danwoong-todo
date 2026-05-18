package com.danwoog.todo.dto.todogroup;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MemberPreviewResponse {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("profile_image")
    private String profileImage;

    public MemberPreviewResponse(Long userId, String profileImage) {
        this.userId = userId;
        this.profileImage = profileImage;
    }

    public Long getUserId() { return userId; }
    public String getProfileImage() { return profileImage; }
}