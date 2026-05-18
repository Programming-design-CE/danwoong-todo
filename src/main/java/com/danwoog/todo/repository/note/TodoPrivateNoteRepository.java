package com.danwoog.todo.repository.note;

import com.danwoog.todo.domain.note.TodoPrivateNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoPrivateNoteRepository extends JpaRepository<TodoPrivateNote, Long> {
}
