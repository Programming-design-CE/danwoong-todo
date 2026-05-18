package com.danwoog.todo.repository.user;

import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.domain.shop.UserCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCharacterRepository extends JpaRepository<UserCharacter, Long> {

    Optional<UserCharacter> findByUser(User user);
}