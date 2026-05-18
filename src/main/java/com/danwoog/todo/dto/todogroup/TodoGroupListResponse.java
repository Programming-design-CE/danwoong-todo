package com.danwoog.todo.dto.todogroup;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TodoGroupListResponse {

    @JsonProperty("groups")
    private List<TodoGroupSummaryResponse> groups;

    public TodoGroupListResponse(List<TodoGroupSummaryResponse> groups) {
        this.groups = groups;
    }

    public List<TodoGroupSummaryResponse> getGroups() {
        return groups;
    }
}