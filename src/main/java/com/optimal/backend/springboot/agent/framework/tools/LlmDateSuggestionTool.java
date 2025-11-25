package com.optimal.backend.springboot.agent.framework.tools;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.core.LlmClient;
import com.optimal.backend.springboot.agent.framework.core.LlmResponse;
import com.optimal.backend.springboot.agent.framework.core.Message;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

@Component
public class LlmDateSuggestionTool {

    @Autowired
    private LlmClient llmClient;
    
    @Autowired
    private GetFutureDateTool getFutureDateTool;

    private static String buildDateSuggestionPrompt(String userCurrentDate) {
        return """
            You are a date parsing assistant that extracts and normalizes dates from user input.
            
            Given a user's description or date reference, determine the appropriate due date and return it in YYYY-MM-DD format.
            
            CRITICAL RULES:
            - The user's current date is: """ + userCurrentDate + """
            - You MUST only return dates that are in the FUTURE (after the current date)
            - If the user mentions a date that has already passed, find the next occurrence of that date
            - Return ONLY the date in YYYY-MM-DD format, nothing else
            - Be PRECISE with date calculations:
              * "in X days" = current date + X days
              * "in X weeks" = current date + (X * 7) days
              * "in X months" = current date + X months (preserve day if possible)
              * "in X years" = current date + X years
              * Calculate these EXACTLY - do not approximate
            - Be intelligent about interpreting natural language date references:
              * "Nov 23" or "November 23" means November 23rd
              * "April 2" or "Apr 2" means April 2nd
              * "December 15" or "Dec 15" means December 15th
              * "by next week" means a date next week (approximately 7 days from now)
              * "next Monday" means the next occurrence of Monday
              * "next Friday" means the next occurrence of Friday
              * "this weekend" means the upcoming Saturday
              * "end of month" means the last day of the current month if future, otherwise next month
              * "end of year" means December 31st of current year if future, otherwise next year
            - If you cannot determine a clear date, return the date 30 days from the current date
            - Your response should be ONLY the date string in YYYY-MM-DD format, no explanation or additional text
            
            Examples:
            Input: "Nov 23" (current date is 2025-11-24)
            Output: 2026-11-23
            
            Input: "April 2" (current date is 2025-11-24)
            Output: 2026-04-02
            
            Input: "in 2 weeks" (current date is 2025-11-24)
            Calculation: 2025-11-24 + 14 days = 2025-12-08
            Output: 2025-12-08
            
            Input: "in 3 weeks" (current date is 2025-11-24)
            Calculation: 2025-11-24 + 21 days = 2025-12-15
            Output: 2025-12-15
            
            Input: "by December 15" (current date is 2025-11-24)
            Output: 2025-12-15
            """;
    }

    @Tool("Parses a date reference from user input and returns an appropriate future due date in YYYY-MM-DD format. " +
          "Handles relative dates (e.g., 'in 3 weeks', 'in 2 months'), absolute dates (e.g., 'Nov 23', 'April 2'), " +
          "and natural language dates (e.g., 'next Monday', 'by December 15'). Uses the user's current date context to ensure the date is in the future.")
    public String SuggestDate(@P("dateInput") String dateInput) {
        System.out.println("\nGETTING DATE SUGGESTION\n");
        
        // Get current date using GetFutureDateTool for consistency
        String currentDateStr = getFutureDateTool.GetFutureDate(0);
        LocalDate currentDate = LocalDate.parse(currentDateStr);
        
        System.out.println("Current date (from GetFutureDateTool): " + currentDateStr);
        System.out.println("Date input: '" + dateInput + "'");
        
        // Check if input is a relative date pattern - if so, calculate and cross-check with GetFutureDateTool
        LocalDate calculatedDate = calculateRelativeDate(dateInput, currentDate);
        if (calculatedDate != null) {
            // Cross-check with GetFutureDateTool for relative dates
            int daysDifference = (int) java.time.temporal.ChronoUnit.DAYS.between(currentDate, calculatedDate);
            String crossCheckDate = getFutureDateTool.GetFutureDate(daysDifference);
            
            if (!calculatedDate.toString().equals(crossCheckDate)) {
                System.out.println("Warning: Direct calculation differs from GetFutureDateTool, using GetFutureDateTool result");
                calculatedDate = LocalDate.parse(crossCheckDate);
            }
            
            System.out.println("Input matched relative date pattern, calculated: " + calculatedDate);
            System.out.println("Final suggested date: " + calculatedDate.toString() + "\n");
            return calculatedDate.toString();
        }
        
        // For non-relative dates (like "Nov 23", "December 15", "next Monday", etc.), use LLM
        String DATE_SUGGEST_PROMPT = buildDateSuggestionPrompt(currentDateStr);
        
        List<Message> contexts = new ArrayList<>();
        contexts.add(new Message("user", dateInput));
        
        // Use lightweight model (empty string defaults to light model)
        LlmResponse response = this.llmClient.generate(DATE_SUGGEST_PROMPT, contexts, "");
        
        String responseContent = response.getContent().trim();
        System.out.println("LLM response: " + responseContent);
        
        // Try to extract and validate the date from the response
        // The LLM should return just YYYY-MM-DD, but we'll try to extract it if there's extra text
        String dateStr = extractDateFromResponse(responseContent);
        
        // Validate the date is in the future
        try {
            LocalDate parsedDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            
            // Ensure the date is in the future
            if (parsedDate.isBefore(currentDate) || parsedDate.isEqual(currentDate)) {
                System.out.println("Parsed date is not in the future, adjusting to next year");
                parsedDate = parsedDate.plusYears(1);
                // Keep adding years if still in the past
                while (parsedDate.isBefore(currentDate)) {
                    parsedDate = parsedDate.plusYears(1);
                }
                dateStr = parsedDate.toString();
            }
            
            System.out.println("Final suggested date: " + parsedDate.toString() + "\n");
            return parsedDate.toString();
            
        } catch (DateTimeParseException e) {
            System.out.println("Failed to parse date from LLM response, defaulting to 30 days from today");
            String defaultDateStr = getFutureDateTool.GetFutureDate(30);
            return defaultDateStr;
        }
    }
    
    /**
     * Calculate relative dates from patterns like "in 3 weeks", "in 2 months", etc.
     * Returns null if the input doesn't match a relative date pattern.
     */
    /**
     * Calculate relative dates from patterns like "in 3 weeks", "in 2 months", etc.
     * Returns null if the input doesn't match a relative date pattern.
     * Handles various formats including "in X days/weeks/months/years", "X days/weeks/months/years", etc.
     */
    private LocalDate calculateRelativeDate(String input, LocalDate baseDate) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        
        String normalized = input.trim().toLowerCase();
        
        // Pattern: "in X days" or "X days" or "in X day" or "X day"
        java.util.regex.Pattern daysPattern = java.util.regex.Pattern.compile("(?:in\\s+)?(\\d+)\\s+days?");
        java.util.regex.Matcher daysMatcher = daysPattern.matcher(normalized);
        if (daysMatcher.find()) {
            int days = Integer.parseInt(daysMatcher.group(1));
            return baseDate.plusDays(days);
        }
        
        // Pattern: "in X weeks" or "X weeks" or "in X week" or "X week"
        java.util.regex.Pattern weeksPattern = java.util.regex.Pattern.compile("(?:in\\s+)?(\\d+)\\s+weeks?");
        java.util.regex.Matcher weeksMatcher = weeksPattern.matcher(normalized);
        if (weeksMatcher.find()) {
            int weeks = Integer.parseInt(weeksMatcher.group(1));
            return baseDate.plusWeeks(weeks);
        }
        
        // Pattern: "in X months" or "X months" or "in X month" or "X month"
        java.util.regex.Pattern monthsPattern = java.util.regex.Pattern.compile("(?:in\\s+)?(\\d+)\\s+months?");
        java.util.regex.Matcher monthsMatcher = monthsPattern.matcher(normalized);
        if (monthsMatcher.find()) {
            int months = Integer.parseInt(monthsMatcher.group(1));
            return baseDate.plusMonths(months);
        }
        
        // Pattern: "in X years" or "X years" or "in X year" or "X year"
        java.util.regex.Pattern yearsPattern = java.util.regex.Pattern.compile("(?:in\\s+)?(\\d+)\\s+years?");
        java.util.regex.Matcher yearsMatcher = yearsPattern.matcher(normalized);
        if (yearsMatcher.find()) {
            int years = Integer.parseInt(yearsMatcher.group(1));
            return baseDate.plusYears(years);
        }
        
        // Pattern: "tomorrow" or "tomorrow's"
        if (normalized.matches("tomorrow'?s?\\b.*")) {
            return baseDate.plusDays(1);
        }
        
        // Pattern: "next week" or "next week's"
        if (normalized.matches("(?:by\\s+)?next\\s+week'?s?\\b.*")) {
            return baseDate.plusWeeks(1);
        }
        
        // Pattern: "next month" or "next month's"
        if (normalized.matches("(?:by\\s+)?next\\s+month'?s?\\b.*")) {
            return baseDate.plusMonths(1);
        }
        
        // Pattern: "next year" or "next year's"
        if (normalized.matches("(?:by\\s+)?next\\s+year'?s?\\b.*")) {
            return baseDate.plusYears(1);
        }
        
        return null; // Not a relative date pattern
    }
    
    /**
     * Extract date in YYYY-MM-DD format from LLM response
     * Handles cases where LLM might add extra text
     */
    private String extractDateFromResponse(String response) {
        // Try to find YYYY-MM-DD pattern in the response
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
        java.util.regex.Matcher matcher = pattern.matcher(response);
        
        if (matcher.find()) {
            return matcher.group(0);
        }
        
        // If no pattern found, return the trimmed response as-is (might be just the date)
        return response.trim();
    }
}

