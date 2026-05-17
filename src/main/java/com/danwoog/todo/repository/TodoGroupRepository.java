package com.danwoog.todo.repository;

import com.danwoog.todo.domain.todogroup.TodoGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoGroupRepository extends JpaRepository<TodoGroup, Long> {
}
