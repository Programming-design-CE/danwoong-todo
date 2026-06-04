package com.danwoog.todo.dto.calendar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Builder
@AllArgsConstructor
public class CalendarTodoDto {
    private Long todoId;
    @JsonProperty("group_id")
    private Long groupId;
    @JsonProperty("group_color")
    private String groupColor;
    private String title;
    private String date;
    @JsonProperty("isCompleted")
    private boolean completed;
    @JsonProperty("group_name")
    private String groupName;
    private String category;
    private String priority;
}
