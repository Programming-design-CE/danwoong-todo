package com.danwoog.todo.repository.todo;

import com.danwoog.todo.domain.note.TodoPrivateNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupTodoPrivateNoteRepository extends JpaRepository<TodoPrivateNote, Long> {
    void deleteByTodo_TodoId(Long todoId);
}
