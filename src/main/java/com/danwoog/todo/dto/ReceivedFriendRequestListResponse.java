package com.danwoog.todo.dto;

import java.util.List;

public class ReceivedFriendRequestListResponse {

    private List<ReceivedFriendRequestResponse> requests;

    public ReceivedFriendRequestListResponse(List<ReceivedFriendRequestResponse> requests) {
        this.requests = requests;
    }

    public List<ReceivedFriendRequestResponse> getRequests() {
        return requests;
    }
}
