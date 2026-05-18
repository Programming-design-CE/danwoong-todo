package com.danwoog.todo.repository;

import com.danwoog.todo.domain.todogroup.TodoGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository
        extends JpaRepository<TodoGroupMember, Long> {

    List<TodoGroupMember> findByUser_UserId(Long userId);

    List<TodoGroupMember> findByGroup_GroupId(Long groupId);

    boolean existsByGroup_GroupIdAndUser_UserId(Long groupId, Long userId);
}