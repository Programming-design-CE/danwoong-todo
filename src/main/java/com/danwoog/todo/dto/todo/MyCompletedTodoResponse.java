package com.danwoog.todo.dto.todo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class MyCompletedTodoResponse {
    private List<MyCompletedTodoDto> todos;
}
