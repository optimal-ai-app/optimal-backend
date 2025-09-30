package com.optimal.backend.springboot.agent.framework.tools;

import java.util.Date;

import com.optimal.backend.springboot.utils.DateUtils;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

public class GetInstructionTool {
    String[] steps;

    public GetInstructionTool(String[] steps) {
        this.steps = steps;
    }

    @Tool("Get specific step in the struction set")
    public String GetInstruction(@P("Step") int step) {
        System.out.println("Providing Instruction: " + steps[step]);
        return steps[step];
    }
}
