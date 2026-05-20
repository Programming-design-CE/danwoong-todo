package com.danwoog.todo.domain.todo;

import java.util.Arrays;

public enum TodoCategory {
    SCHOOL("학교"),
    EXTRACURRICULAR("대외활동"),
    STUDY("스터디"),
    PERSONAL("개인"),
    ETC("기타");

    private final String label;

    TodoCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static TodoCategory from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        return Arrays.stream(values())
                .filter(category ->
                        category.name().equalsIgnoreCase(normalized)
                                || category.label.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카테고리입니다: " + value));
    }
}
