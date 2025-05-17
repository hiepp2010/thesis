package com.example.chatservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @GetMapping("/public/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Chat service is running");
    }
    
    @GetMapping("/public/test")
    public ResponseEntity<Map<String, String>> publicTest() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This is a public test endpoint");
        response.put("service", "chat-service");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello(Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello from Chat Service!");
        if (authentication != null) {
            response.put("username", authentication.getName());
        }
        return ResponseEntity.ok(response);
    }
}
