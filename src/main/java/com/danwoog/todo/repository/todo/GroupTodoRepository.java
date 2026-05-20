package com.danwoog.todo.repository.todo;

import com.danwoog.todo.domain.todo.GarlicDistributionType;
import com.danwoog.todo.domain.todo.Todo;
import com.danwoog.todo.domain.todo.TodoStatus;
import com.danwoog.todo.domain.todogroup.Priority;
import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.dto.todo.GroupTodoDetailView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GroupTodoRepository extends JpaRepository<Todo, Long> {

    @Query("""
            SELECT t
            FROM Todo t
            JOIN FETCH t.group g
            WHERE g.groupId = :groupId
              AND (:status IS NULL OR t.status = :status)
            ORDER BY t.deadline ASC, t.todoId ASC
            """)
    List<Todo> findGroupTodosByStatus(
            @Param("groupId") Long groupId,
            @Param("status") TodoStatus status
    );

    @Query("""
            SELECT new com.danwoog.todo.dto.todo.GroupTodoDetailView(
                t.todoId,
                t.todoName,
                t.description,
                t.deadline,
                t.priority,
                t.status,
                t.garlicReward,
                t.category,
                t.distributionType
            )
            FROM Todo t
            WHERE t.todoId = :todoId
            """)
    Optional<GroupTodoDetailView> findDetailViewByTodoId(@Param("todoId") Long todoId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Todo t
            SET t.todoName = COALESCE(:todoName, t.todoName),
                t.description = COALESCE(:description, t.description),
                t.deadline = COALESCE(:deadline, t.deadline),
                t.garlicReward = COALESCE(:garlicReward, t.garlicReward),
                t.priority = COALESCE(:priority, t.priority),
                t.category = COALESCE(:category, t.category),
                t.distributionType = COALESCE(:distributionType, t.distributionType),
                t.updatedAt = :updatedAt
            WHERE t.todoId = :todoId
            """)
    int updateTodoFields(
            @Param("todoId") Long todoId,
            @Param("todoName") String todoName,
            @Param("description") String description,
            @Param("deadline") LocalDateTime deadline,
            @Param("garlicReward") Integer garlicReward,
            @Param("priority") Priority priority,
            @Param("category") String category,
            @Param("distributionType") GarlicDistributionType distributionType,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Todo t
            SET t.status = 'COMPLETED',
                t.completedBy = :completedBy,
                t.completedAt = :completedAt,
                t.updatedAt = :updatedAt
            WHERE t.todoId = :todoId
            """)
    int completeTodo(
            @Param("todoId") Long todoId,
            @Param("completedBy") User completedBy,
            @Param("completedAt") LocalDateTime completedAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Todo t
            SET t.status = 'IN_PROGRESS',
                t.completedBy = NULL,
                t.completedAt = NULL,
                t.updatedAt = :updatedAt
            WHERE t.todoId = :todoId
            """)
    int reopenTodo(
            @Param("todoId") Long todoId,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}
