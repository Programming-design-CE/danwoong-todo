package com.danwoog.todo.repository;

<<<<<<< HEAD
import com.danwoog.todo.domain.User;
=======
import com.danwoog.todo.domain.user.User;
>>>>>>> main
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByLoginId(String loginId);

    Optional<User> findByLoginId(String loginId);
}