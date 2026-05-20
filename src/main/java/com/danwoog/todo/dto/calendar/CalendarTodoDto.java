package com.danwoog.todo.dto.calendar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class CalendarTodoDto {
    private Long todoId;
    private String title;
    private LocalDate date;
    @JsonProperty("isCompleted")
    private boolean completed;
    private String category;
    private String priority;
}
