package com.danwoog.todo.controller;

import com.danwoog.todo.dto.user.LoginRequest;
import com.danwoog.todo.dto.user.LoginResponse;
import com.danwoog.todo.dto.user.SignupRequest;
import com.danwoog.todo.dto.user.UpdateUserRequest;
import com.danwoog.todo.dto.user.UpdateUserResponse;
import com.danwoog.todo.dto.user.UserInfoResponse;
import com.danwoog.todo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "User API", description = "회원가입, 로그인, 내 정보 조회/수정 API")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "회원가입", description = "로그인 ID, 비밀번호, 닉네임으로 회원가입합니다.")
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(@RequestBody SignupRequest request) {
        userService.signup(request);
    }

    @Operation(summary = "로그인", description = "로그인 성공 시 access token과 refresh token을 반환합니다.")
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @Operation(summary = "내 정보 조회", description = "JWT 토큰을 기반으로 현재 로그인한 사용자의 정보를 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    public UserInfoResponse getMyInfo(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userService.getMyInfo(userId);
    }

    @Operation(summary = "내 캐릭터 조회", description = "JWT 토큰을 기반으로 현재 로그인한 사용자의 캐릭터 정보를 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/character")
    public Map<String, Object> getMyCharacter(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();

        return Map.of(
                "character_id", 1L,
                "user_id", userId,
                "equipped_items", List.of()
        );
    }

    @Operation(summary = "내 정보 수정", description = "JWT 토큰을 기반으로 현재 로그인한 사용자의 닉네임을 수정합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping
    public UpdateUserResponse updateMyInfo(
            Authentication authentication,
            @RequestBody UpdateUserRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return userService.updateMyInfo(userId, request);
    }

    @Operation(summary = "마늘 누적 추가", description = "JWT 토큰을 기반으로 현재 로그인한 사용자의 마늘 개수를 누적 추가합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/garlic")
    public UserInfoResponse addGarlic(
            Authentication authentication,
            @RequestBody Map<String, Integer> request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        int amount = request.getOrDefault("amount", 0);

        return userService.addGarlic(userId, amount);
    }
}