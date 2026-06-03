package com.danwoog.todo.repository;

import com.danwoog.todo.domain.todogroup.TodoGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository
        extends JpaRepository<TodoGroupMember, Long> {

    List<TodoGroupMember> findByUser_UserId(Long userId);

    List<TodoGroupMember> findByGroup_GroupId(Long groupId);

    boolean existsByGroup_GroupIdAndUser_UserId(Long groupId, Long userId);

    Optional<TodoGroupMember> findByGroup_GroupIdAndUser_UserId(Long groupId, Long userId);
}
