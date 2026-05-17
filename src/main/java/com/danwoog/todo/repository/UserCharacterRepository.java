package com.danwoog.todo.repository;

import com.danwoog.todo.domain.User;
import com.danwoog.todo.domain.UserCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCharacterRepository extends JpaRepository<UserCharacter, Long> {

    Optional<UserCharacter> findByUser(User user);
}