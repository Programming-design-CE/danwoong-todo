package com.danwoog.todo.repository.todo;

import com.danwoog.todo.domain.todo.TodoAssignee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupTodoAssigneeRepository extends JpaRepository<TodoAssignee, Long> {

    @Query("""
            SELECT ta
            FROM TodoAssignee ta
            JOIN FETCH ta.user u
            WHERE ta.todo.todoId IN :todoIds
            ORDER BY ta.assigneeId ASC
            """)
    List<TodoAssignee> findByTodoIdsWithUser(@Param("todoIds") List<Long> todoIds);

    @Query("""
            SELECT ta
            FROM TodoAssignee ta
            JOIN FETCH ta.user u
            WHERE ta.todo.todoId = :todoId
            ORDER BY ta.assigneeId ASC
            """)
    List<TodoAssignee> findByTodoIdWithUser(@Param("todoId") Long todoId);

    void deleteByTodo_TodoId(Long todoId);
}
