package com.optimal.backend.springboot.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.optimal.backend.springboot.agent.framework.agents.ExampleAgent;
import com.optimal.backend.springboot.agent.framework.core.BaseSupervisor;
import com.optimal.backend.springboot.agent.framework.core.Message;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ExampleAgent exampleAgent;

    @Autowired
    private BaseSupervisor supervisor;

    @PostMapping
    public ResponseEntity<String> chat(@RequestBody Map<String, Object> request) {
        supervisor.addAgent("exampleAgent", exampleAgent);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> messages = (List<Map<String, Object>>) request.get("messages");

        List<Message> convertedMessages = messages.stream()
                .map(msg -> new Message(
                        (String) msg.get("role"),
                        (String) msg.get("content")))
                .toList();

        String output = supervisor.execute(convertedMessages);
        System.out.println("output: " + output);
        return ResponseEntity.ok(output);
    }
}
