package com.danwoog.todo.service;

import com.danwoog.todo.domain.User;
import com.danwoog.todo.dto.LoginRequest;
import com.danwoog.todo.dto.LoginResponse;
import com.danwoog.todo.dto.SignupRequest;
import com.danwoog.todo.dto.UpdateUserRequest;
import com.danwoog.todo.dto.UpdateUserResponse;
import com.danwoog.todo.dto.UserInfoResponse;
import com.danwoog.todo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // Lombok 안 쓰고 생성자 직접 주입
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 회원가입
    @Transactional
    public void signup(SignupRequest request) {

        if (userRepository.existsByLoginId(request.getLogin_id())) {
            throw new IllegalArgumentException("이미 존재하는 로그인 ID입니다.");
        }

        User user = new User(
                request.getLogin_id(),
                request.getPassword(),
                request.getNickname()
        );

        userRepository.save(user);
    }

    // 로그인
    @Transactional
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByLoginId(request.getLogin_id())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = "access_token_" + UUID.randomUUID();
        String refreshToken = "refresh_token_" + UUID.randomUUID();

        return new LoginResponse(accessToken, refreshToken);
    }

    // 내 정보 조회
    public UserInfoResponse getMyInfo(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return new UserInfoResponse(
                user.getUserId(),
                user.getNickname(),
                user.getGarlicCount(),
                user.getProfileImage()
        );
    }

    // 내 정보 수정
    @Transactional
    public UpdateUserResponse updateMyInfo(Long userId, UpdateUserRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.updateNickname(request.getNickname());

        return new UpdateUserResponse(
                user.getUserId(),
                user.getNickname()
        );
    }
}