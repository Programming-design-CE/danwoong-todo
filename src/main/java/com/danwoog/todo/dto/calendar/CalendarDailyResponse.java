package com.danwoog.todo.dto.calendar;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CalendarDailyResponse {
    private String date;
    private int count;
    private List<CalendarTodoDto> todos;
}
