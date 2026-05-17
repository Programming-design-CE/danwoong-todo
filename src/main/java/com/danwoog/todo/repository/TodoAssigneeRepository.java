package com.danwoog.todo.repository;

import com.danwoog.todo.domain.TodoAssignee;
import com.danwoog.todo.domain.TodoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TodoAssigneeRepository extends JpaRepository<TodoAssignee, Long> {
    
    @Query("SELECT ta FROM TodoAssignee ta JOIN FETCH ta.todo t LEFT JOIN FETCH t.group g WHERE ta.member.id = :memberId AND ta.status = :status")
    List<TodoAssignee> findByMemberIdAndStatusWithTodoAndGroup(@Param("memberId") Long memberId, @Param("status") TodoStatus status);
    
    @Query("SELECT ta FROM TodoAssignee ta JOIN FETCH ta.todo t WHERE ta.member.id = :memberId")
    List<TodoAssignee> findByMemberIdWithTodo(@Param("memberId") Long memberId);
}
