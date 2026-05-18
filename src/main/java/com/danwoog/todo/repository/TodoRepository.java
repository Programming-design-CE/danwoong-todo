package com.danwoog.todo.repository;

import com.danwoog.todo.domain.todo.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByDeadlineBetween(LocalDateTime start, LocalDateTime end);
}
