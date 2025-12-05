package com.optimal.backend.springboot.agent.framework.tools;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.core.UserContext;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

@Component
public class GetFutureDateTool {

    @Tool("Get the future date based on the user's current date")
    public String GetFutureDate(@P("days") int days) {
        // Get user's current date from context (uses their timezone)
        LocalDate userDate = UserContext.getUserLocalDate();
        LocalDate futureDate = userDate.plusDays(days);

        return futureDate.toString(); // Returns yyyy-MM-dd format
    }
}
