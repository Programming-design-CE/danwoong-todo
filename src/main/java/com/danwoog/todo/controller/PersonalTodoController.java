package com.danwoog.todo.controller;

import com.danwoog.todo.dto.note.MyNoteRequest;
import com.danwoog.todo.dto.note.MyNoteResponse;
import com.danwoog.todo.dto.todo.MyCompletedTodoResponse;
import com.danwoog.todo.dto.todo.MyTodoResponse;
import com.danwoog.todo.dto.todo.MyTodoStatisticsResponse;
import com.danwoog.todo.service.PersonalTodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/todos/my")
public class PersonalTodoController {

    private final PersonalTodoService personalTodoService;

    @GetMapping
    public ResponseEntity<MyTodoResponse> getMyTodos(Authentication authentication) {
        return ResponseEntity.ok(personalTodoService.getMyTodos(getLoginUserId(authentication)));
    }

    @GetMapping("/completed")
    public ResponseEntity<MyCompletedTodoResponse> getMyCompletedTodos(Authentication authentication) {
        return ResponseEntity.ok(personalTodoService.getMyCompletedTodos(getLoginUserId(authentication)));
    }

    @GetMapping("/note")
    public ResponseEntity<MyNoteResponse> getMyNote(Authentication authentication) {
        return ResponseEntity.ok(personalTodoService.getMyNote(getLoginUserId(authentication)));
    }

    @PutMapping("/note")
    public ResponseEntity<Void> updateMyNote(Authentication authentication, @RequestBody MyNoteRequest request) {
        personalTodoService.updateMyNote(getLoginUserId(authentication), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<MyTodoStatisticsResponse> getMyStatistics(Authentication authentication) {
        return ResponseEntity.ok(personalTodoService.getMyStatistics(getLoginUserId(authentication)));
    }

    private Long getLoginUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
