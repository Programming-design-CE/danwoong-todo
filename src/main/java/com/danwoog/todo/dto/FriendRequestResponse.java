package com.danwoog.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "친구 요청 응답")
public class FriendRequestResponse {

    @Schema(description = "친구 요청 ID", example = "1")
    private Long request_id;

    @Schema(description = "요청 상태", example = "PENDING")
    private String status;

    public FriendRequestResponse(Long request_id, String status) {
        this.request_id = request_id;
        this.status = status;
    }

    public Long getRequest_id() {
        return request_id;
    }

    public String getStatus() {
        return status;
    }
}
