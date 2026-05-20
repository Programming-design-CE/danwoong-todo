package com.danwoog.todo.dto.todogroup;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MemberPreviewResponse {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("profile_image")
    private String profileImage;

    @JsonProperty("nickname")
    private String nickname;

    public MemberPreviewResponse(Long userId, String nickname, String profileImage) {
        this.userId = userId;
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    public Long getUserId() { return userId; }
    public String getNickname() { return nickname; }
    public String getProfileImage() { return profileImage; }
}
