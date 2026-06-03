package com.danwoog.todo.dto.friend;

import java.util.List;

public class SentFriendRequestListResponse {

    private List<SentFriendRequestResponse> requests;

    public SentFriendRequestListResponse(List<SentFriendRequestResponse> requests) {
        this.requests = requests;
    }

    public List<SentFriendRequestResponse> getRequests() {
        return requests;
    }
}
