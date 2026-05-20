package com.danwoog.todo.controller;

import com.danwoog.todo.dto.note.MyNoteRequest;
import com.danwoog.todo.dto.note.MyNoteResponse;
import com.danwoog.todo.dto.todo.MyCompletedTodoResponse;
import com.danwoog.todo.dto.todo.MyTodoResponse;
import com.danwoog.todo.dto.todo.MyTodoStatisticsResponse;
import com.danwoog.todo.service.PersonalTodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/todos/my")
public class PersonalTodoController {

    private final PersonalTodoService personalTodoService;
    
    // 임시로 하드코딩된 사용자 ID
    private final Long TEMP_MEMBER_ID = 1L;

    @GetMapping
    public ResponseEntity<MyTodoResponse> getMyTodos() {
        return ResponseEntity.ok(personalTodoService.getMyTodos(TEMP_MEMBER_ID));
    }

    @GetMapping("/completed")
    public ResponseEntity<MyCompletedTodoResponse> getMyCompletedTodos() {
        return ResponseEntity.ok(personalTodoService.getMyCompletedTodos(TEMP_MEMBER_ID));
    }

    @GetMapping("/note")
    public ResponseEntity<MyNoteResponse> getMyNote() {
        return ResponseEntity.ok(personalTodoService.getMyNote(TEMP_MEMBER_ID));
    }

    @PutMapping("/note")
    public ResponseEntity<Void> updateMyNote(@RequestBody MyNoteRequest request) {
        personalTodoService.updateMyNote(TEMP_MEMBER_ID, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<MyTodoStatisticsResponse> getMyStatistics() {
        return ResponseEntity.ok(personalTodoService.getMyStatistics(TEMP_MEMBER_ID));
    }
}
