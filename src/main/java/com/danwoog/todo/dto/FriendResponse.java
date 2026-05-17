package com.danwoog.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "친구 정보")
public class FriendResponse {

    @Schema(description = "사용자 ID", example = "2")
    private Long user_id;

    @Schema(description = "닉네임", example = "세훈")
    private String nickname;

    @Schema(description = "캐릭터 썸네일 URL", example = "image_url")
    private String character_thumbnail_url;

    public FriendResponse(Long user_id, String nickname, String character_thumbnail_url) {
        this.user_id = user_id;
        this.nickname = nickname;
        this.character_thumbnail_url = character_thumbnail_url;
    }

    public Long getUser_id() {
        return user_id;
    }

    public String getNickname() {
        return nickname;
    }

    public String getCharacter_thumbnail_url() {
        return character_thumbnail_url;
    }
}