package com.danwoog.todo.controller;

import com.danwoog.todo.dto.todogroup.TodoGroupCreateRequest;
import com.danwoog.todo.dto.todogroup.TodoGroupCreateResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupDeleteResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupInviteRequest;
import com.danwoog.todo.dto.todogroup.TodoGroupInviteResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupListResponse;
import com.danwoog.todo.dto.todogroup.TodoGroupUpdateRequest;
import com.danwoog.todo.dto.todogroup.TodoGroupUpdateResponse;
import com.danwoog.todo.service.TodoGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
            summary = "공동 할 일 그룹 생성 및 멤버 바로 추가",
            description = "공동 할 일 그룹을 생성하고 invitee_ids 사용자를 바로 그룹 멤버로 추가합니다."
    )
    @PostMapping
    public TodoGroupCreateResponse createGroup(
            Authentication authentication,
            @RequestBody TodoGroupCreateRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();

        // Swagger 테스트용 로그
        System.out.println("[POST /todo-groups] 현재 로그인 userId = " + userId);
        System.out.println("[POST /todo-groups] inviteeIds = " + request.getInviteeIds());

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

        // Swagger 테스트용 로그
        System.out.println("[GET /todo-groups] 현재 로그인 userId = " + userId);

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

        // Swagger 테스트용 로그
        System.out.println("[PATCH /todo-groups/" + groupId + "] 현재 로그인 userId = " + userId);

        return todoGroupService.updateGroup(userId, groupId, request);
    }

    @Operation(
            summary = "공동 할 일 그룹 삭제",
            description = "공동 할 일 그룹을 삭제합니다."
    )
    @DeleteMapping("/{groupId}")
    public TodoGroupDeleteResponse deleteGroup(
            Authentication authentication,
            @PathVariable("groupId") Long groupId
    ) {
        Long userId = (Long) authentication.getPrincipal();

        // Swagger 테스트용 로그
        System.out.println("[DELETE /todo-groups/" + groupId + "] 현재 로그인 userId = " + userId);

        return todoGroupService.deleteGroup(userId, groupId);
    }



    @Operation(
            summary = "완료된 프로젝트의 마늘 분배",
            description = "팀장이 완료된 프로젝트의 마늘을 멤버들에게 분배합니다."
    )
    @PostMapping("/{groupId}/garlic-distribution")
    public void distributeGarlic(
            Authentication authentication,
            @PathVariable("groupId") Long groupId,
            @RequestBody com.danwoog.todo.dto.todogroup.TodoGroupGarlicDistributionRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        todoGroupService.distributeGarlic(userId, groupId, request);
    }

    @Operation(
                summary = "그룹 인원 추가",
                description = "member_ids 사용자들을 그룹 멤버로 바로 추가합니다."
        )
        @PostMapping("/{groupId}/invitations")
        public TodoGroupInviteResponse inviteMembers(
                Authentication authentication,
                @Parameter(description = "그룹 ID")
                @PathVariable("groupId") Long groupId,
                @RequestBody TodoGroupInviteRequest request
        ) {
        Long userId = (Long) authentication.getPrincipal();

        return todoGroupService.inviteMembers(
                userId,
                groupId,
                request
        );
        }
}