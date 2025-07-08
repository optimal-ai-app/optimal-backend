package com.optimal.backend.springboot.agent.framework.agents;

import com.optimal.backend.springboot.agent.framework.agents.prompts.TaskAgentPrompt;
import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import com.optimal.backend.springboot.agent.framework.core.LlmClient;
import com.optimal.backend.springboot.agent.framework.tools.GoalDescriptionTool;
import com.optimal.backend.springboot.agent.framework.tools.GetTasksforGoalTool;
import com.optimal.backend.springboot.agent.framework.tools.TaskAgentCreateTaskTool;
import com.optimal.backend.springboot.agent.framework.tools.DeleteTaskTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class TaskAgent extends BaseAgent {
    private final TaskAgentCreateTaskTool createTaskTool;
    private final GoalDescriptionTool goalDescriptionTool;
    private final GetTasksforGoalTool getTasksforGoalTool;
    private final DeleteTaskTool deleteTaskTool;

    @Autowired
    public TaskAgent(
            @Value("${langchain4j.task-agent.name}") String name,
            @Value("${langchain4j.task-agent.description}") String description,
            TaskAgentCreateTaskTool createTaskTool,
            GoalDescriptionTool goalDescriptionTool,
            GetTasksforGoalTool getTasksforGoalTool,
            DeleteTaskTool deleteTaskTool,
            LlmClient llmClient) {
        super(name, description, TaskAgentPrompt.getDefaultPrompt(), llmClient);
        this.createTaskTool = createTaskTool;
        this.goalDescriptionTool = goalDescriptionTool;
        this.getTasksforGoalTool = getTasksforGoalTool;
        this.deleteTaskTool = deleteTaskTool;

        addTool(createTaskTool);
        addTool(goalDescriptionTool);
        addTool(getTasksforGoalTool);
        addTool(deleteTaskTool);
    }

    @PostConstruct
    @Override
    protected void initialize() {
        System.out.println("TaskAgent initialized with tools: " + getTools().size());
        getTools().forEach(tool -> System.out.println("- " + tool.getName() + ": " + tool.getDescription()));
    }
}
