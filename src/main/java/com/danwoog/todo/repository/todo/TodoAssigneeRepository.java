package com.danwoog.todo.repository.todo;

import com.danwoog.todo.domain.todo.TodoAssignee;
import com.danwoog.todo.domain.todo.TodoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TodoAssigneeRepository extends JpaRepository<TodoAssignee, Long> {
    
    @Query("SELECT ta FROM TodoAssignee ta JOIN FETCH ta.todo t LEFT JOIN FETCH t.group g WHERE ta.user.userId = :memberId AND t.status = :status")
    List<TodoAssignee> findByMemberIdAndStatusWithTodoAndGroup(@Param("memberId") Long memberId, @Param("status") TodoStatus status);
    
    @Query("SELECT ta FROM TodoAssignee ta JOIN FETCH ta.todo t WHERE ta.user.userId = :memberId")
    List<TodoAssignee> findByMemberIdWithTodo(@Param("memberId") Long memberId);
}
