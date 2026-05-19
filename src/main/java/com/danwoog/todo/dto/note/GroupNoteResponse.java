package com.danwoog.todo.dto.note;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GroupNoteResponse {

    @JsonProperty("group_note_id")
    private final Long groupNoteId;

    @JsonProperty("content")
    private final String content;

    public GroupNoteResponse(Long groupNoteId, String content) {
        this.groupNoteId = groupNoteId;
        this.content = content;
    }

    public Long getGroupNoteId() {
        return groupNoteId;
    }

    public String getContent() {
        return content;
    }
}
