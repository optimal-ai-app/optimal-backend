package com.optimal.backend.springboot.agent.framework.agents;

import com.optimal.backend.springboot.agent.framework.agents.prompts.TaskAgentPrompt;
import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import com.optimal.backend.springboot.agent.framework.tools.GoalDescriptionTool;
import com.optimal.backend.springboot.agent.framework.tools.GetTasksforGoalTool;
import com.optimal.backend.springboot.agent.framework.tools.TaskAgentCreateTaskTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class TaskAgent extends BaseAgent {
    @Autowired
    private TaskAgentCreateTaskTool createTaskTool;

    @Autowired
    private GoalDescriptionTool goalDescriptionTool;

    @Autowired
    private GetTasksforGoalTool getTasksforGoalTool;

    public TaskAgent() {
        super("TaskAgent",
                "Can perform CRUD (only create tasks for now) operations for a user's tasks by understanding "
                        + "the user's goals and generating tasks based on the user's goals."
                        + "can read the user's goals and generate tasks based on the user's goals.",
                TaskAgentPrompt.getDefaultPrompt());
    }

    @PostConstruct
    @Override
    protected void initialize() {
        // Add tools after Spring dependency injection is complete
        addTool(createTaskTool);
        addTool(goalDescriptionTool);
        addTool(getTasksforGoalTool);

        System.out.println("TaskAgent initialized with tools: " + getTools().size());
        getTools().forEach(tool -> System.out.println("- " + tool.getName() + ": " + tool.getDescription()));
    }
}
