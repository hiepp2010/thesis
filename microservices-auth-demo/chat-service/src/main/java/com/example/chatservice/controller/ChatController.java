package com.example.chatservice.controller;

import com.example.chatservice.config.JwtUtil;
import com.example.chatservice.dto.ConversationResponse;
import com.example.chatservice.dto.DirectConversationRequest;
import com.example.chatservice.dto.MessageRequest;
import com.example.chatservice.dto.MessageResponse;
import com.example.chatservice.model.Message;
import com.example.chatservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final JwtUtil jwtUtil;
    
    // Temporary in-memory storage for testing
    private final Map<String, List<MessageResponse>> conversations = new HashMap<>();

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
    public ResponseEntity<Map<String, String>> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello from Chat Service!");
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            response.put("username", auth.getName());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Chat service is up and running!");
    }
    
    // Simple test endpoint for creating a direct chat
    @PostMapping("/public/direct-chat")
    public ResponseEntity<Map<String, String>> createDirectChat(
            @RequestParam String user1, 
            @RequestParam String user2) {
        String conversationId = user1 + "-" + user2;
        
        if (!conversations.containsKey(conversationId)) {
            conversations.put(conversationId, new ArrayList<>());
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("conversationId", conversationId);
        response.put("user1", user1);
        response.put("user2", user2);
        
        return ResponseEntity.ok(response);
    }
    
    // Simple test endpoint for sending a message
    @PostMapping("/public/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @RequestParam String conversationId,
            @RequestParam String senderId,
            @RequestParam String content) {
        
        if (!conversations.containsKey(conversationId)) {
            conversations.put(conversationId, new ArrayList<>());
        }
        
        MessageResponse message = MessageResponse.builder()
                .id(String.valueOf(System.currentTimeMillis()))
                .conversationId(conversationId)
                .senderId(senderId)
                .content(content)
                .contentType(Message.MessageType.TEXT)
                .sentAt(LocalDateTime.now())
                .edited(false)
                .build();
        
        conversations.get(conversationId).add(message);
        
        return ResponseEntity.ok(message);
    }
    
    // Simple test endpoint for getting messages
    @GetMapping("/public/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @RequestParam String conversationId) {
        
        List<MessageResponse> messages = conversations.getOrDefault(conversationId, new ArrayList<>());
        
        return ResponseEntity.ok(messages);
    }
    
    // PUBLIC ENDPOINT FOR TOKEN INSPECTION
    @GetMapping("/public/inspect-token")
    public ResponseEntity<Map<String, Object>> inspectToken(@RequestParam String token) {
        try {
            // Extract all claims from token
            Map<String, Object> result = jwtUtil.extractAllClaimsAsMap(token);
            
            // Add token itself
            result.put("rawToken", token);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to process token");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/conversations/direct")
    public ResponseEntity<ConversationResponse> createDirectConversation(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody DirectConversationRequest request) {
        String token = authHeader.substring(7);
        String userId = jwtUtil.extractUsername(token);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.createDirectConversation(userId, request));
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getUserConversations(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String userId = jwtUtil.extractUsername(token);
        
        return ResponseEntity.ok(chatService.getUserConversations(userId));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Page<MessageResponse>> getConversationMessages(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String token = authHeader.substring(7);
        String userId = jwtUtil.extractUsername(token);
        
        return ResponseEntity.ok(chatService.getConversationMessages(
                conversationId, userId, PageRequest.of(page, size)));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String conversationId,
            @RequestBody MessageRequest request) {
        String token = authHeader.substring(7);
        String userId = jwtUtil.extractUsername(token);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.sendMessage(conversationId, userId, request));
    }

    @PutMapping("/messages/{messageId}")
    public ResponseEntity<MessageResponse> updateMessage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String messageId,
            @RequestBody MessageRequest request) {
        String token = authHeader.substring(7);
        String userId = jwtUtil.extractUsername(token);
        
        return ResponseEntity.ok(chatService.updateMessage(messageId, userId, request));
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String messageId) {
        String token = authHeader.substring(7);
        String userId = jwtUtil.extractUsername(token);
        
        chatService.deleteMessage(messageId, userId);
        return ResponseEntity.noContent().build();
    }
}
