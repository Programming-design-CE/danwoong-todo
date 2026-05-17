package com.danwoog.todo.controller;

import com.danwoog.todo.dto.LoginRequest;
import com.danwoog.todo.dto.LoginResponse;
import com.danwoog.todo.dto.SignupRequest;
import com.danwoog.todo.dto.UpdateUserRequest;
import com.danwoog.todo.dto.UpdateUserResponse;
import com.danwoog.todo.dto.UserInfoResponse;
import com.danwoog.todo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // Lombok 안 쓰고 생성자 직접 주입
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 회원가입
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(@RequestBody SignupRequest request) {
        userService.signup(request);
    }

    // 로그인
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    // 내 정보 조회
    @GetMapping
    public UserInfoResponse getMyInfo() {
        Long userId = 1L; // TODO: 나중에 JWT 적용하면 로그인한 사용자 ID로 변경
        return userService.getMyInfo(userId);
    }

    // 내 캐릭터 조회
    // 아직 캐릭터 테이블/서비스 연결 안 했으니까 임시 응답
    @GetMapping("/character")
    public Map<String, Object> getMyCharacter() {
        Long userId = 1L; // TODO: 나중에 JWT 적용하면 로그인한 사용자 ID로 변경

        return Map.of(
                "character_id", 1L,
                "user_id", userId,
                "equipped_items", List.of()
        );
    }

    // 내 정보 수정
    @PatchMapping
    public UpdateUserResponse updateMyInfo(@RequestBody UpdateUserRequest request) {
        Long userId = 1L; // TODO: 나중에 JWT 적용하면 로그인한 사용자 ID로 변경
        return userService.updateMyInfo(userId, request);
    }
}