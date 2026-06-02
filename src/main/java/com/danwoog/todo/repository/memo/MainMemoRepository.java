package com.danwoog.todo.repository.memo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.danwoog.todo.domain.memo.MainMemo;

public interface MainMemoRepository extends JpaRepository<MainMemo, Long> {
    List<MainMemo> findByUserUserIdOrderByCreatedAtDesc(Long userId);
}