package com.danwoog.todo.service;

import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.dto.user.LoginRequest;
import com.danwoog.todo.dto.user.LoginResponse;
import com.danwoog.todo.dto.user.SignupRequest;
import com.danwoog.todo.dto.user.UpdateUserRequest;
import com.danwoog.todo.dto.user.UpdateUserResponse;
import com.danwoog.todo.dto.user.UserInfoResponse;
import com.danwoog.todo.global.security.JwtProvider;
import com.danwoog.todo.repository.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByLoginId(request.getLogin_id())) {
            throw new IllegalArgumentException("이미 존재하는 로그인 ID입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getLogin_id(),
                encodedPassword,
                request.getNickname()
        );

        userRepository.save(user);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.getLogin_id())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(user.getUserId());
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId());

        return new LoginResponse(accessToken, refreshToken);
    }

    public UserInfoResponse getMyInfo(Long userId) {
        User user = findUser(userId);

        return new UserInfoResponse(
                user.getUserId(),
                user.getNickname(),
                user.getGarlicCount(),
                user.getProfileImage()
        );
    }

    @Transactional
    public UpdateUserResponse updateMyInfo(Long userId, UpdateUserRequest request) {
        User user = findUser(userId);

        user.updateNickname(request.getNickname());

        return new UpdateUserResponse(
                user.getUserId(),
                user.getNickname()
        );
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}