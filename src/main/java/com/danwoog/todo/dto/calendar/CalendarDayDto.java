package com.danwoog.todo.dto.calendar;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class CalendarDayDto {
    private LocalDate date;
    private int count;
}
