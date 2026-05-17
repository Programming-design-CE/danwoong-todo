package com.danwoog.todo.repository;

import com.danwoog.todo.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByLoginId(String loginId);
    List<User> findByNicknameContaining(String keyword);
    Optional<User> findByLoginId(String loginId);
}