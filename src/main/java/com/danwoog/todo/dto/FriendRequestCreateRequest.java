package com.danwoog.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "친구 요청 생성 요청")
public class FriendRequestCreateRequest {

    @Schema(description = "친구 요청을 받을 사용자 ID", example = "2")
    private Long receiver_id;

    public FriendRequestCreateRequest() {
    }

    public Long getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_id(Long receiver_id) {
        this.receiver_id = receiver_id;
    }
}
