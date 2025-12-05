package com.optimal.backend.springboot.agent.framework.tools;

import dev.langchain4j.agent.tool.Tool;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Tool for tracking and managing a queue of milestones to be created.
 * Each TaskCreatorAgent instance gets its own tool instance with a specific milestone list.
 */
public class MilestoneQueueTool {
    private final Queue<MilestoneData> remainingMilestones;
    
    public MilestoneQueueTool(Queue<MilestoneData> milestones) {
        this.remainingMilestones = new LinkedList<>(milestones);
        System.out.println("MilestoneQueueTool initialized with " + milestones.size() + " milestones");
    }
    
    @Tool("Returns the next milestone that needs to be created. Returns 'EMPTY' if all milestones have been created.")
    public String getNextMilestone() {
        if (remainingMilestones.isEmpty()) {
            return "EMPTY - all milestones have been created";
        }
        
        MilestoneData next = remainingMilestones.peek();
        System.out.println("→ Next milestone: " + next.title + " (remaining: " + remainingMilestones.size() + ")");
        
        return String.format(
            "Title: '%s'%n" +
            "Description: %s%n" +
            "Due Date: %s%n" +
            "Goal: '%s'", 
            next.title, 
            next.description, 
            next.dueDate, 
            next.goalName
        );
    }
    
    @Tool("Marks the current milestone as created and removes it from the queue. Call this after the user confirms milestone creation.")
    public String markMilestoneCreated() {
        if (remainingMilestones.isEmpty()) {
            return "ERROR: No milestones in queue";
        }
        
        MilestoneData created = remainingMilestones.poll();
        int remaining = remainingMilestones.size();
        
        System.out.println("✓ Milestone created: '" + created.title + "' (remaining: " + remaining + ")");
        
        if (remaining == 0) {
            return String.format("Milestone '%s' marked as created. All milestones complete!", created.title);
        }
        
        return String.format("Milestone '%s' marked as created. %d milestone(s) remaining.", 
            created.title, remaining);
    }
    
    @Tool("Returns the count of remaining milestones in the queue. Returns '0' when all milestones have been created.")
    public String getRemainingCount() {
        return String.valueOf(remainingMilestones.size());
    }
    
    /**
     * Data class representing a single milestone
     */
    public static class MilestoneData {
        public String title;
        public String description;
        public String dueDate;
        public String goalName;
        
        public MilestoneData(String title, String description, String dueDate, String goalName) {
            this.title = title;
            this.description = description;
            this.dueDate = dueDate;
            this.goalName = goalName;
        }
        
        @Override
        public String toString() {
            return String.format("Milestone{title='%s', due='%s', goal='%s'}", title, dueDate, goalName);
        }
    }
}

