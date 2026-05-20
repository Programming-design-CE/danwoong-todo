package com.danwoog.todo.dto.todo;

import com.danwoog.todo.domain.todo.TodoCategory;
import lombok.Getter;

@Getter
public class CategorySummaryDto {
    private final String category;
    private final Integer total_count;
    private final Integer completed_count;

    public CategorySummaryDto(TodoCategory category, Integer total_count, Integer completed_count) {
        this.category = category != null ? category.getLabel() : null;
        this.total_count = total_count;
        this.completed_count = completed_count;
    }
}
