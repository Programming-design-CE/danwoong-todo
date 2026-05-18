package com.danwoog.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategorySummaryDto {
    private String category;
    private Integer total_count;
    private Integer completed_count;
}
