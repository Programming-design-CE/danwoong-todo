package com.danwoog.todo.controller;

import com.danwoog.todo.dto.todogroup.*;
import com.danwoog.todo.service.TodoGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Todo Group API", description = "공동 할 일 그룹 API")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping("/todo-groups")
public class TodoGroupController {

    private final TodoGroupService todoGroupService;

    @Operation(
            summary = "공동 할 일 그룹 생성 및 친구 초대",
            description = "공동 할 일 그룹을 생성하고 친구를 초대합니다."
    )
    @PostMapping
    public TodoGroupCreateResponse createGroup(
            Authentication authentication,
            @RequestBody TodoGroupCreateRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return todoGroupService.createGroup(userId, request);
    }

    @Operation(
            summary = "내가 속한 공동 할 일 그룹 목록 조회",
            description = "현재 로그인한 사용자가 속한 그룹 목록을 조회합니다."
    )
    @GetMapping
    public TodoGroupListResponse getMyGroups(
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return todoGroupService.getMyGroups(userId);
    }




    @Operation(
            summary = "그룹 정보 수정",
            description = "공동 할 일 그룹 정보를 수정합니다."
    )
    @PatchMapping("/{groupId}")
    public TodoGroupUpdateResponse updateGroup(
            Authentication authentication,
            @PathVariable("groupId") Long groupId,
            @RequestBody TodoGroupUpdateRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return todoGroupService.updateGroup(userId, groupId, request);
    }





    @Operation(
            summary = "공동 할 일 그룹 삭제",
            description = "공동 할 일 그룹을 삭제합니다."
    )
    @DeleteMapping("/{groupId}")
    public TodoGroupDeleteResponse deleteGroup(
            Authentication authentication,
            @PathVariable Long groupId
    ) {
        Long userId = (Long) authentication.getPrincipal();

        return todoGroupService.deleteGroup(userId, groupId);
    }
}