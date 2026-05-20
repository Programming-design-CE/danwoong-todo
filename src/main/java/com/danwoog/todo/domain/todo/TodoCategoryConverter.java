package com.danwoog.todo.domain.todo;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TodoCategoryConverter implements AttributeConverter<TodoCategory, String> {

    @Override
    public String convertToDatabaseColumn(TodoCategory category) {
        if (category == null) return null;
        return category.name();
    }

    @Override
    public TodoCategory convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        try {
            return TodoCategory.from(dbData);
        } catch (IllegalArgumentException e) {
            return null; // 깨진 데이터는 null로 처리
        }
    }
}