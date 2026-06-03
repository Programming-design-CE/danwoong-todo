package com.danwoog.todo.dto.friend;

public class SentFriendRequestResponse {

    private Long request_id;
    private Long receiver_id;
    private String nickname;
    private String status;

    public SentFriendRequestResponse(Long request_id, Long receiver_id, String nickname, String status) {
        this.request_id = request_id;
        this.receiver_id = receiver_id;
        this.nickname = nickname;
        this.status = status;
    }

    public Long getRequest_id() {
        return request_id;
    }

    public Long getReceiver_id() {
        return receiver_id;
    }

    public String getNickname() {
        return nickname;
    }

    public String getStatus() {
        return status;
    }
}
