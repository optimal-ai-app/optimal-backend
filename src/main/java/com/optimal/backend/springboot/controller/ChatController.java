package com.optimal.backend.springboot.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.optimal.backend.springboot.agent.framework.agents.ExampleAgent;
import com.optimal.backend.springboot.agent.framework.core.Message;


@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ExampleAgent exampleAgent;
/*
 *  private String role;
    private String message;
    private String content;
 */
    @PostMapping
    public ResponseEntity<String> chat(@RequestBody String message) {
        List<Message> response = exampleAgent.run(Arrays.asList(new Message("user", message)));
        StringBuilder sb = new StringBuilder("Output:\n");

        for(Message msg : response) {
            sb.append(msg.getRole() + ": " + msg.getMessage() + "\n");
        }
        return ResponseEntity.ok(sb.toString());
    }
}
