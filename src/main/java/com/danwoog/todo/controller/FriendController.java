package com.danwoog.todo.controller;

import com.danwoog.todo.dto.*;
import com.danwoog.todo.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Friend API", description = "친구 요청, 친구 목록, 친구 검색 API")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/friends")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @Operation(summary = "친구 요청 보내기", description = "다른 사용자에게 친구 요청을 보냅니다.")
    @PostMapping("/requests")
    public FriendRequestResponse sendFriendRequest(
            Authentication authentication,
            @RequestBody FriendRequestCreateRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return friendService.sendFriendRequest(userId, request);
    }

    @Operation(summary = "친구 목록 조회", description = "현재 로그인한 사용자의 친구 목록을 조회합니다.")
    @GetMapping
    public FriendListResponse getFriends(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return friendService.getFriends(userId);
    }

    @Operation(summary = "받은 친구 요청 조회", description = "현재 로그인한 사용자가 받은 대기 중인 친구 요청을 조회합니다.")
    @GetMapping("/requests")
    public ReceivedFriendRequestListResponse getReceivedRequests(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return friendService.getReceivedRequests(userId);
    }

    @Operation(summary = "친구 요청 승인", description = "받은 친구 요청을 승인합니다.")
    @PatchMapping("/requests/{requestId}/accept")
    public FriendRequestResponse acceptRequest(
            Authentication authentication,
            @PathVariable Long requestId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return friendService.acceptRequest(userId, requestId);
    }

    @Operation(summary = "친구 요청 거절", description = "받은 친구 요청을 거절합니다.")
    @PatchMapping("/requests/{requestId}/reject")
    public FriendRequestResponse rejectRequest(
            Authentication authentication,
            @PathVariable Long requestId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return friendService.rejectRequest(userId, requestId);
    }

    @Operation(summary = "사용자 검색", description = "닉네임 키워드로 사용자를 검색합니다.")
    @GetMapping("/search")
    public UserSearchResponse searchUsers(
            Authentication authentication,
            @RequestParam String keyword
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return friendService.searchUsers(userId, keyword);
    }
}