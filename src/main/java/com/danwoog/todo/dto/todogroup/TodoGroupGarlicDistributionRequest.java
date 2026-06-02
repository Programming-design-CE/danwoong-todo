package com.danwoog.todo.dto.todogroup;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TodoGroupGarlicDistributionRequest {
    
    @JsonProperty("distributions")
    private List<GarlicDistributionDto> distributions;
    
    public List<GarlicDistributionDto> getDistributions() {
        return distributions;
    }
}
