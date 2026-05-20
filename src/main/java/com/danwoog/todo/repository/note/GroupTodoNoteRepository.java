package com.danwoog.todo.repository.note;

import com.danwoog.todo.domain.note.GroupNote;
import com.danwoog.todo.dto.note.GroupNoteResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface GroupTodoNoteRepository extends JpaRepository<GroupNote, Long> {

    boolean existsByGroup_GroupId(Long groupId);

    @Query("""
            SELECT new com.danwoog.todo.dto.note.GroupNoteResponse(
                gn.groupNoteId,
                gn.content
            )
            FROM GroupNote gn
            WHERE gn.group.groupId = :groupId
            """)
    Optional<GroupNoteResponse> findResponseByGroupId(@Param("groupId") Long groupId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE GroupNote gn
            SET gn.content = :content,
                gn.updatedAt = :updatedAt
            WHERE gn.group.groupId = :groupId
            """)
    int updateContentByGroupId(
            @Param("groupId") Long groupId,
            @Param("content") String content,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}
