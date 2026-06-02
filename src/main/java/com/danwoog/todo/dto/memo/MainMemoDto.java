package com.danwoog.todo.dto.memo;

import com.danwoog.todo.domain.memo.MainMemo;

import lombok.Getter;

@Getter
public class MainMemoDto {
    private Long memo_id;
    private String content;

    public MainMemoDto(MainMemo memo) {
        this.memo_id = memo.getMemoId();
        this.content = memo.getContent();
    }
}