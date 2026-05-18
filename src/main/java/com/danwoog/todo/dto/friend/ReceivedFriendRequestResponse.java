package com.danwoog.todo.dto.friend;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "받은 친구 요청 정보")
public class ReceivedFriendRequestResponse {

    @Schema(description = "친구 요청 ID", example = "1")
    private Long request_id;

    @Schema(description = "요청 보낸 사용자 ID", example = "2")
    private Long sender_id;

    @Schema(description = "요청 보낸 사용자 닉네임", example = "세훈")
    private String nickname;

    @Schema(description = "요청 상태", example = "PENDING")
    private String status;

    public ReceivedFriendRequestResponse(Long request_id, Long sender_id, String nickname, String status) {
        this.request_id = request_id;
        this.sender_id = sender_id;
        this.nickname = nickname;
        this.status = status;
    }

    public Long getRequest_id() {
        return request_id;
    }

    public Long getSender_id() {
        return sender_id;
    }

    public String getNickname() {
        return nickname;
    }

    public String getStatus() {
        return status;
    }
}
