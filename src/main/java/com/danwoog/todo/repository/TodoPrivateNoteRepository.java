package com.danwoog.todo.repository;

import com.danwoog.todo.domain.TodoPrivateNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoPrivateNoteRepository extends JpaRepository<TodoPrivateNote, Long> {
}
