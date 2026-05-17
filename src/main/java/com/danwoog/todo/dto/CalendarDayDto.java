package com.danwoog.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class CalendarDayDto {
    private LocalDate date;
    private int count;
}
