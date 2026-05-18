package com.danwoog.todo.dto.friend;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "친구 목록 응답")
public class FriendListResponse {

    private List<FriendResponse> friends;

    public FriendListResponse(List<FriendResponse> friends) {
        this.friends = friends;
    }

    public List<FriendResponse> getFriends() {
        return friends;
    }
}