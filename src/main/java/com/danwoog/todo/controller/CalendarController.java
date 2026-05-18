package com.danwoog.todo.controller;

import com.danwoog.todo.dto.calendar.CalendarDailyResponse;
import com.danwoog.todo.dto.calendar.CalendarMonthlyResponse;
import com.danwoog.todo.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/todos")
    public CalendarDailyResponse getDailyTodos(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return calendarService.getDailyTodos(date);
    }

    @GetMapping("/month")
    public CalendarMonthlyResponse getMonthlyTodos(
            @RequestParam("year") int year,
            @RequestParam("month") int month) {
        return calendarService.getMonthlyTodos(year, month);
    }
}
