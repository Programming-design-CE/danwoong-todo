package com.danwoog.todo.controller;

import com.danwoog.todo.dto.calendar.CalendarDailyResponse;
import com.danwoog.todo.dto.calendar.CalendarMonthlyResponse;
import com.danwoog.todo.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
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
            Authentication authentication,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "view", defaultValue = "assigned") String viewMode) {
        return calendarService.getDailyTodos(getLoginUserId(authentication), date, viewMode);
    }

    @GetMapping("/month")
    public CalendarMonthlyResponse getMonthlyTodos(
            Authentication authentication,
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @RequestParam(name = "view", defaultValue = "assigned") String viewMode) {
        return calendarService.getMonthlyTodos(getLoginUserId(authentication), year, month, viewMode);
    }

    private Long getLoginUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
