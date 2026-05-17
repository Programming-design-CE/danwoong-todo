package com.danwoog.todo.repository;

import com.danwoog.todo.domain.todogroup.TodoGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<TodoGroupMember, Long> {
}
