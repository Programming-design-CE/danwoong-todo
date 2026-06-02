package com.danwoog.todo.repository.todo;

import com.danwoog.todo.domain.todo.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByDeadlineBetween(LocalDateTime start, LocalDateTime end);
    
    int countByGroup_GroupId(Long groupId);
    int countByGroup_GroupIdAndStatus(Long groupId, com.danwoog.todo.domain.todo.TodoStatus status);
}
