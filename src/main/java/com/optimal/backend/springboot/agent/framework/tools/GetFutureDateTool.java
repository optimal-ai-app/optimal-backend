package com.optimal.backend.springboot.agent.framework.tools;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.utils.DateUtils;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

@Component
public class GetFutureDateTool {

    @Tool("Get the future date")
    public String GetFutureDate(@P("days") int days) {
        Date futureDate = DateUtils.getCurrentDatePlusDays(days);
        return futureDate.toString();
    }
}
