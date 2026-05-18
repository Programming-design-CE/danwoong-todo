package com.danwoog.todo.repository;

<<<<<<< HEAD
import com.danwoog.todo.domain.RefreshToken;
=======
import com.danwoog.todo.domain.user.RefreshToken;
>>>>>>> main
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
}