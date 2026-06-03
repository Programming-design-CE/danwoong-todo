package com.danwoog.todo.repository;

import com.danwoog.todo.domain.todogroup.TodoGroup;
import com.danwoog.todo.domain.todogroup.GroupStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoGroupRepository extends JpaRepository<TodoGroup, Long> {

    List<TodoGroup> findByStatus(GroupStatus status);
}
