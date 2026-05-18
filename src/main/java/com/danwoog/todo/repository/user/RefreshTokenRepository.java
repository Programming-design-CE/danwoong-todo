package com.danwoog.todo.repository.user;

import com.danwoog.todo.domain.user.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
}