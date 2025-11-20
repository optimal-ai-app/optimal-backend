package com.optimal.backend.springboot.agent.framework.agents;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.agents.prompts.MilestoneTaskCreatorPrompt;
import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import com.optimal.backend.springboot.agent.framework.core.LlmClient;
import com.optimal.backend.springboot.agent.framework.core.Message;
import com.optimal.backend.springboot.agent.framework.tools.GetFutureDateTool;
import com.optimal.backend.springboot.agent.framework.tools.MilestoneQueueTool;

import jakarta.annotation.PostConstruct;

@Component
@Scope("prototype")
public class MilestoneTaskCreatorAgent extends BaseAgent {
    private GetFutureDateTool getFutureDateTool;
    private MilestoneQueueTool milestoneQueueTool;
    private boolean milestoneToolInitialized = false;

    @Autowired
    public MilestoneTaskCreatorAgent(
            LlmClient llmClient, GetFutureDateTool getFutureDateTool) {
        super("MilestoneTaskCreatorAgent",
                "Creates milestone tasks based on milestone planning data from MilestonePlannerAgent. Specializes exclusively in milestone task creation.",
                MilestoneTaskCreatorPrompt.getDefaultPrompt(), llmClient);
        this.getFutureDateTool = getFutureDateTool;
        addTool(this.getFutureDateTool);
    }

    @PostConstruct
    @Override
    protected void initialize() {
        System.out.println("MilestoneTaskCreatorAgent initialized with tools: " + getTools().size());
        getTools().forEach(tool -> System.out.println("- " + tool.getClass().getSimpleName()));
    }

    @Override
    public List<Message> run(List<Message> instructions) {
        // On first run, check if we need to initialize milestone tool from context
        if (!milestoneToolInitialized && shouldInitializeMilestoneTool(instructions)) {
            initializeMilestoneToolFromContext(instructions);
            milestoneToolInitialized = true;
        }
        
        return super.run(instructions);
    }

    /**
     * Check if context contains milestone list from MilestonePlannerAgent
     */
    private boolean shouldInitializeMilestoneTool(List<Message> context) {
        for (Message msg : context) {
            String content = msg.getTextContent();
            if (content != null && content.contains("milestones that need to be created")) {
                System.out.println("[MilestoneTaskCreatorAgent] Detected milestone list in context");
                return true;
            }
        }
        return false;
    }

    /**
     * Parse milestone list from context and initialize the MilestoneQueueTool
     */
    private void initializeMilestoneToolFromContext(List<Message> context) {
        Queue<MilestoneQueueTool.MilestoneData> milestones = parseMilestoneList(context);
        
        if (!milestones.isEmpty()) {
            this.milestoneQueueTool = new MilestoneQueueTool(milestones);
            addTool(this.milestoneQueueTool);
            System.out.println("✓ MilestoneTaskCreatorAgent: Initialized MilestoneQueueTool with " + milestones.size() + " milestones");
        } else {
            System.out.println("⚠ MilestoneTaskCreatorAgent: No milestones found in context");
        }
    }

    /**
     * Parse milestone list from MilestonePlannerAgent's output
     * Expected format: "These are the milestones that need to be created: 'M1 by date1, M2 by date2, M3 by date3' for goal 'GoalName'."
     */
    private Queue<MilestoneQueueTool.MilestoneData> parseMilestoneList(List<Message> context) {
        Queue<MilestoneQueueTool.MilestoneData> milestones = new LinkedList<>();
        
        for (Message msg : context) {
            String content = msg.getTextContent();
            if (content == null || !content.contains("milestones that need to be created")) {
                continue;
            }
            
            System.out.println("[MilestoneTaskCreatorAgent] Parsing milestone list from: " + content);
            
            // Extract goal name
            String goalName = extractGoalName(content);
            System.out.println("[MilestoneTaskCreatorAgent] Goal name: " + goalName);
            
            // Extract milestone strings
            List<String> milestoneStrings = extractMilestoneStrings(content);
            System.out.println("[MilestoneTaskCreatorAgent] Found " + milestoneStrings.size() + " milestones");
            
            // Parse each milestone
            for (String ms : milestoneStrings) {
                MilestoneQueueTool.MilestoneData data = parseSingleMilestone(ms, goalName);
                if (data != null) {
                    milestones.add(data);
                    System.out.println("[MilestoneTaskCreatorAgent] Parsed milestone: " + data);
                }
            }
            
            break;
        }
        
        return milestones;
    }

    /**
     * Extract goal name from content
     * Pattern: "for goal 'GoalName'"
     */
    private String extractGoalName(String content) {
        Pattern goalPattern = Pattern.compile("for goal ['\"]([^'\"]+)['\"]");
        Matcher goalMatcher = goalPattern.matcher(content);
        if (goalMatcher.find()) {
            return goalMatcher.group(1);
        }
        return "Unknown Goal";
    }

    /**
     * Extract individual milestone strings from content
     * Pattern: "milestones that need to be created: 'M1 by date1, M2 by date2, M3 by date3'"
     */
    private List<String> extractMilestoneStrings(String content) {
        List<String> milestoneStrings = new LinkedList<>();
        
        // Find content between quotes after "milestones that need to be created:"
        Pattern milestonePattern = Pattern.compile("milestones that need to be created: ['\"]([^'\"]+)['\"]");
        Matcher milestoneMatcher = milestonePattern.matcher(content);
        
        if (milestoneMatcher.find()) {
            String milestonesText = milestoneMatcher.group(1);
            // Split by comma, but be careful with commas in descriptions
            // For now, assume format: "Title by date, Title by date, Title by date"
            String[] parts = milestonesText.split(",(?=\\s*[A-Z])"); // Split on comma followed by space and capital letter
            
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    milestoneStrings.add(trimmed);
                }
            }
        }
        
        return milestoneStrings;
    }

    /**
     * Parse a single milestone string
     * Expected format: "Milestone title by YYYY-MM-DD" or "Milestone title by date"
     */
    private MilestoneQueueTool.MilestoneData parseSingleMilestone(String milestoneString, String goalName) {
        // Pattern to extract title and date: "Title by Date"
        Pattern pattern = Pattern.compile("^(.+?)\\s+by\\s+(.+)$");
        Matcher matcher = pattern.matcher(milestoneString.trim());
        
        if (matcher.find()) {
            String title = matcher.group(1).trim();
            String dueDate = matcher.group(2).trim();
            
            // Description is same as title for now
            String description = title + " by " + dueDate;
            
            return new MilestoneQueueTool.MilestoneData(title, description, dueDate, goalName);
        }
        
        System.out.println("⚠ Could not parse milestone: " + milestoneString);
        return null;
    }
}

