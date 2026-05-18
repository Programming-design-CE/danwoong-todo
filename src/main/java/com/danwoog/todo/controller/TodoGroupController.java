package com.danwoog.todo.controller;

import com.danwoog.todo.dto.todogroup.TodoGroupCreateRequest;
import com.danwoog.todo.dto.todogroup.TodoGroupCreateResponse;
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

    @Operation(summary = "공동 할 일 그룹 생성 및 친구 초대", description = "공동 할 일 그룹을 생성하고 친구를 초대합니다.")
    @PostMapping
    public TodoGroupCreateResponse createGroup(
            Authentication authentication,
            @RequestBody TodoGroupCreateRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return todoGroupService.createGroup(userId, request);
    }
}