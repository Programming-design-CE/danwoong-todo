package com.danwoog.todo.controller;

import com.danwoog.todo.domain.todo.TodoStatus;
import com.danwoog.todo.dto.note.GroupNoteRequest;
import com.danwoog.todo.dto.note.GroupNoteResponse;
import com.danwoog.todo.dto.todo.GroupTodoCompleteResponse;
import com.danwoog.todo.dto.todo.GroupTodoCreateRequest;
import com.danwoog.todo.dto.todo.GroupTodoCreateResponse;
import com.danwoog.todo.dto.todo.GroupTodoDetailResponse;
import com.danwoog.todo.dto.todo.GroupTodoListResponse;
import com.danwoog.todo.dto.todo.GroupTodoUpdateRequest;
import com.danwoog.todo.dto.todo.GroupTodoUpdateResponse;
import com.danwoog.todo.service.GroupTodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Todo Group Todo API", description = "공동 할 일 및 그룹 메모 API")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequiredArgsConstructor
public class TodoGroupTodoController {

    private final GroupTodoService groupTodoService;

    @Operation(summary = "공동 세부 할 일 추가", description = "담당자별 마늘 분배 정보와 함께 공동 세부 할 일을 추가합니다.")
    @PostMapping("/todo-groups/{groupId}/todos")
    public GroupTodoCreateResponse createTodo(
            Authentication authentication,
            @Parameter(description = "그룹 ID")
            @PathVariable("groupId") Long groupId,
            @RequestBody GroupTodoCreateRequest request
    ) {
        return groupTodoService.createTodo(getLoginUserId(authentication), groupId, request);
    }

    @Operation(summary = "공동 할 일 목록 조회", description = "특정 그룹의 공동 할 일을 상태별로 조회합니다.")
    @GetMapping("/todo-groups/{groupId}/todos")
    public GroupTodoListResponse getGroupTodos(
            Authentication authentication,
            @Parameter(description = "그룹 ID")
            @PathVariable("groupId") Long groupId,
            @Parameter(description = "할 일 상태")
            @RequestParam(name = "status", required = false) TodoStatus status
    ) {
        return groupTodoService.getGroupTodos(getLoginUserId(authentication), groupId, status);
    }

    @Operation(summary = "공동 할 일 상세 조회", description = "공동 할 일의 상세 정보와 담당자별 마늘 보상을 조회합니다.")
    @GetMapping("/todos/{todoId}")
    public GroupTodoDetailResponse getTodoDetail(
            Authentication authentication,
            @Parameter(description = "할 일 ID")
            @PathVariable("todoId") Long todoId
    ) {
        return groupTodoService.getTodoDetail(getLoginUserId(authentication), todoId);
    }

    @Operation(summary = "공동 할 일 수정", description = "담당자별 마늘 분배 정보를 포함해 공동 할 일을 수정합니다.")
    @PatchMapping("/todos/{todoId}")
    public GroupTodoUpdateResponse updateTodo(
            Authentication authentication,
            @Parameter(description = "할 일 ID")
            @PathVariable("todoId") Long todoId,
            @RequestBody GroupTodoUpdateRequest request
    ) {
        return groupTodoService.updateTodo(getLoginUserId(authentication), todoId, request);
    }

    @Operation(summary = "공동 할 일 완료 처리", description = "공동 할 일을 완료 상태로 변경하고 담당자별 마늘 보상을 지급합니다.")
    @PatchMapping("/todos/{todoId}/complete")
    public GroupTodoCompleteResponse completeTodo(
            Authentication authentication,
            @Parameter(description = "할 일 ID")
            @PathVariable("todoId") Long todoId
    ) {
        return groupTodoService.completeTodo(getLoginUserId(authentication), todoId);
    }

    @Operation(summary = "공동 할 일 삭제", description = "공동 할 일을 삭제합니다.")
    @DeleteMapping("/todos/{todoId}")
    public ResponseEntity<Void> deleteTodo(
            Authentication authentication,
            @Parameter(description = "할 일 ID")
            @PathVariable("todoId") Long todoId
    ) {
        groupTodoService.deleteTodo(getLoginUserId(authentication), todoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "그룹 별 메모장 내용 조회", description = "특정 그룹의 메모장 내용을 조회합니다.")
    @GetMapping("/todo-groups/{groupId}/note")
    public GroupNoteResponse getGroupNote(
            Authentication authentication,
            @Parameter(description = "그룹 ID")
            @PathVariable("groupId") Long groupId
    ) {
        return groupTodoService.getGroupNote(getLoginUserId(authentication), groupId);
    }

    @Operation(summary = "그룹 메모장 내용 등록", description = "특정 그룹의 메모장 내용을 등록하거나 수정합니다.")
    @PutMapping("/todo-groups/{groupId}/note")
    public GroupNoteResponse updateGroupNote(
            Authentication authentication,
            @Parameter(description = "그룹 ID")
            @PathVariable("groupId") Long groupId,
            @RequestBody GroupNoteRequest request
    ) {
        return groupTodoService.upsertGroupNote(getLoginUserId(authentication), groupId, request);
    }

    private Long getLoginUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
