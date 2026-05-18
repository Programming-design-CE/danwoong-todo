package com.danwoog.todo.dto;

public class UserInfoResponse {

    private Long user_id;
    private String nickname;
    private Integer garlic_count;
    private String character_thumbnail_url;

    public UserInfoResponse(Long user_id, String nickname, Integer garlic_count, String character_thumbnail_url) {
        this.user_id = user_id;
        this.nickname = nickname;
        this.garlic_count = garlic_count;
        this.character_thumbnail_url = character_thumbnail_url;
    }

    public Long getUser_id() {
        return user_id;
    }

    public String getNickname() {
        return nickname;
    }

    public Integer getGarlic_count() {
        return garlic_count;
    }

    public String getCharacter_thumbnail_url() {
        return character_thumbnail_url;
    }
}
