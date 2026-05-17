package com.danwoog.todo.repository;

import com.danwoog.todo.domain.todo.TodoAssignee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TodoAssigneeRepository extends JpaRepository<TodoAssignee, Long> {
    
    @Query("SELECT ta FROM TodoAssignee ta JOIN FETCH ta.todo t JOIN FETCH t.group g WHERE ta.user.id = :userId AND t.status = :status")
    List<TodoAssignee> findByUserIdAndStatusWithTodoAndGroup(@Param("userId") Long userId, @Param("status") String status);
    
    @Query("SELECT ta FROM TodoAssignee ta JOIN FETCH ta.todo t WHERE ta.user.id = :userId")
    List<TodoAssignee> findByUserIdWithTodo(@Param("userId") Long userId);
}
