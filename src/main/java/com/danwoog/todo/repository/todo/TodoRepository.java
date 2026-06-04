package com.danwoog.todo.repository.todo;

import com.danwoog.todo.domain.todo.Todo;
import com.danwoog.todo.domain.todogroup.GroupStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByDeadlineBetween(LocalDateTime start, LocalDateTime end);

    @Query("""
            SELECT DISTINCT t
            FROM TodoAssignee ta
            JOIN ta.todo t
            JOIN FETCH t.group g
            WHERE ta.user.userId = :userId
              AND t.deadline BETWEEN :start AND :end
              AND g.status <> :deletedStatus
            """)
    List<Todo> findCalendarTodosByUserIdAndDeadlineBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("deletedStatus") GroupStatus deletedStatus
    );

    @Query("""
            SELECT DISTINCT t
            FROM Todo t
            JOIN FETCH t.group g
            JOIN TodoGroupMember gm ON gm.group = g
            WHERE gm.user.userId = :userId
              AND t.deadline BETWEEN :start AND :end
              AND g.status <> :deletedStatus
            """)
    List<Todo> findCalendarTodosByGroupMemberAndDeadlineBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("deletedStatus") GroupStatus deletedStatus
    );
    
    int countByGroup_GroupId(Long groupId);
    int countByGroup_GroupIdAndStatus(Long groupId, com.danwoog.todo.domain.todo.TodoStatus status);
}
