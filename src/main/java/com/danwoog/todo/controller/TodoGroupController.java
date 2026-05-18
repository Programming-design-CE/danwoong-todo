package com.danwoog.todo.controller;

import com.danwoog.todo.dto.todogroup.TodoGroupCreateRequest;
import com.danwoog.todo.dto.todogroup.TodoGroupCreateResponse;
import com.danwoog.todo.service.TodoGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/todo-groups")
public class TodoGroupController {

    private final TodoGroupService todoGroupService;

    @PostMapping
    public TodoGroupCreateResponse createGroup(
            @RequestBody TodoGroupCreateRequest request
    ) {
        Long loginUserId = 1L;

        return todoGroupService.createGroup(loginUserId, request);
    }
}