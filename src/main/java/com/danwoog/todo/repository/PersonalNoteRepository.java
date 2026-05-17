package com.danwoog.todo.repository;

import com.danwoog.todo.domain.todo.PersonalNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonalNoteRepository extends JpaRepository<PersonalNote, Long> {
    Optional<PersonalNote> findByUserId(Long userId);
}
