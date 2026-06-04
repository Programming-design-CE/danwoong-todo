package com.danwoog.todo.controller;

import com.danwoog.todo.dto.memo.MainMemoCreateRequest;
import com.danwoog.todo.dto.memo.MainMemoDto;
import com.danwoog.todo.dto.memo.MainMemoListResponse;
import com.danwoog.todo.service.MainMemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/main/memo")
public class MainMemoController {

    private final MainMemoService mainMemoService;

    @GetMapping
    public ResponseEntity<MainMemoListResponse> getMemos(Authentication authentication) {
        return ResponseEntity.ok(mainMemoService.getMemos(getLoginUserId(authentication)));
    }

    @PostMapping
    public ResponseEntity<MainMemoDto> createMemo(
            Authentication authentication,
            @RequestBody MainMemoCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mainMemoService.createMemo(getLoginUserId(authentication), request));
    }

    @PutMapping("/{memoId}")
    public ResponseEntity<MainMemoDto> updateMemo(
            Authentication authentication,
            @PathVariable Long memoId,
            @RequestBody MainMemoCreateRequest request) {
        return ResponseEntity.ok(mainMemoService.updateMemo(getLoginUserId(authentication), memoId, request));
    }

    @DeleteMapping("/{memoId}")
    public ResponseEntity<Void> deleteMemo(
            Authentication authentication,
            @PathVariable Long memoId) {
        mainMemoService.deleteMemo(getLoginUserId(authentication), memoId);
        return ResponseEntity.noContent().build();
    }

    private Long getLoginUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}