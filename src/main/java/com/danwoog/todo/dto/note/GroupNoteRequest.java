package com.danwoog.todo.dto.note;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GroupNoteRequest {

    @JsonProperty("content")
    private String content;

    public String getContent() {
        return content;
    }
}
