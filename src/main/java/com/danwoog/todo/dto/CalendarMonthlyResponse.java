package com.danwoog.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CalendarMonthlyResponse {
    private int year;
    private int month;
    private List<CalendarDayDto> days;
}
