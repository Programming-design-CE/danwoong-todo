package com.danwoog.todo.global;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileSchemaInitializer {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void updateFileColumnLengths() {
        alterColumnLength("original_name", 255);
        alterColumnLength("stored_name", 255);
        alterColumnLength("file_url", 255);
        alterColumnLength("file_type", 255);
    }

    private void alterColumnLength(String columnName, int length) {
        jdbcTemplate.execute(
                "ALTER TABLE files ALTER COLUMN " + columnName + " VARCHAR(" + length + ")"
        );
    }
}
