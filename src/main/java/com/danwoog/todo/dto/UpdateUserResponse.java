package com.danwoog.todo.dto;

public class UpdateUserResponse {

    private Long user_id;
    private String nickname;

    public UpdateUserResponse(Long user_id, String nickname) {
        this.user_id = user_id;
        this.nickname = nickname;
    }

    public Long getUser_id() {
        return user_id;
    }

    public String getNickname() {
        return nickname;
    }
}