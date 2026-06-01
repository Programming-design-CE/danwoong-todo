package com.danwoog.todo.dto.memo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class MainMemoListResponse {
    private List<MainMemoDto> memos;
}