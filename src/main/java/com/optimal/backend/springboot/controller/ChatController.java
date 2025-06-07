package com.optimal.backend.springboot.controller;

import java.util.Arrays;
import java.util.List;

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
    public ResponseEntity<String> chat(@RequestBody String message) {
        supervisor.addAgent("exampleAgent", exampleAgent);
        return ResponseEntity.ok(supervisor.execute(message));
    }
}
