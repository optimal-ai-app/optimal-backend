// src/main/java/com/optimal/backend/springboot/domain/service/SummarizationClient.java
package com.optimal.backend.springboot.service;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SummarizationClient {

    /**
     * Given the existing summary and a list of messages ("role: content"),
     * returns an updated conversational summary.
     */
    public String updateSummary(String existingSummary, List<String> latestMessages) {
        // Simple implementation - just keep track of message count and latest messages
        if (latestMessages == null || latestMessages.isEmpty()) {
            return existingSummary != null ? existingSummary : "";
        }
        
        StringBuilder summary = new StringBuilder();
        if (existingSummary != null && !existingSummary.trim().isEmpty()) {
            summary.append(existingSummary).append(" ");
        }
        
        summary.append("Conversation with ").append(latestMessages.size()).append(" messages. ");
        
        // Add the last few messages as context
        int startIndex = Math.max(0, latestMessages.size() - 3);
        summary.append("Recent messages: ");
        for (int i = startIndex; i < latestMessages.size(); i++) {
            if (i > startIndex) {
                summary.append("; ");
            }
            String message = latestMessages.get(i);
            // Truncate long messages
            if (message.length() > 100) {
                message = message.substring(0, 97) + "...";
            }
            summary.append(message);
        }
        
        return summary.toString();
    }
}