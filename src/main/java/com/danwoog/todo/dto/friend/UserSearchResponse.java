package com.danwoog.todo.dto.friend;


import java.util.List;

public class UserSearchResponse {

    private List<FriendResponse> users;

    public UserSearchResponse(List<FriendResponse> users) {
        this.users = users;
    }

    public List<FriendResponse> getUsers() {
        return users;
    }
}
