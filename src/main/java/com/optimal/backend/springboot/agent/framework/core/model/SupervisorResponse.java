package com.optimal.backend.springboot.agent.framework.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class SupervisorResponse {
    @Builder.Default
    public String content = "";
    @Builder.Default
    public List<String> tags = new ArrayList<>();
    public boolean readyToHandoff;
    @Builder.Default
    public Map<String, Object> data = new HashMap<>();
    public boolean reInterpret;
    public int currentStep;

    public SupervisorResponse(String content, List<String> tags, boolean readyToHandoff, Map<String, Object> data,
            boolean reInterpret, int currentStep) {
        this.content = content;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.readyToHandoff = readyToHandoff;
        this.data = data != null ? data : new HashMap<>();
        this.reInterpret = reInterpret;
        this.currentStep = currentStep;
    }
}
