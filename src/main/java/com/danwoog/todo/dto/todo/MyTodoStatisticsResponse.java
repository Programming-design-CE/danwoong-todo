package com.danwoog.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class MyTodoStatisticsResponse {
    private Integer progress_rate;
    private Integer expected_garlic;
    private List<CategorySummaryDto> category_summary;
}
