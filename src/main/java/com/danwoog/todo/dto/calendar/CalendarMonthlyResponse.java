package com.danwoog.todo.dto.calendar;

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
