package com.example.chatservice.controller;

import com.example.chatservice.model.ChatMessage;
import com.example.chatservice.service.ChatService;
import com.example.chatservice.dto.MessageRequest;
import com.example.chatservice.dto.DirectConversationRequest;
import com.example.chatservice.dto.ConversationResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WebSocketChatController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketChatController.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage, 
                                  SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            logger.error("No authenticated principal found for message send");
            return null;
        }
        
        String authenticatedUsername = principal.getName();
        logger.info("Authenticated user sending message: {}", authenticatedUsername);
        
        if (!authenticatedUsername.equals(chatMessage.getSender())) {
            logger.error("Username mismatch in chat message. Token: {}, Message: {}", 
                authenticatedUsername, chatMessage.getSender());
            return null;
        }
        
        chatMessage.setTimestamp(LocalDateTime.now().format(formatter));
        
        // Log message to terminal
        logger.info("CHAT MESSAGE: {} - {}: {}", 
            chatMessage.getTimestamp(), 
            chatMessage.getSender(), 
            chatMessage.getContent());
        
        // Also print to console for direct terminal visibility
        System.out.println("CHAT MESSAGE: " + chatMessage.getTimestamp() + 
            " - " + chatMessage.getSender() + ": " + chatMessage.getContent());
        
        // Check if this is a direct conversation message
        if (chatMessage.getConversationId() != null) {
            // Route to specific conversation topic
            messagingTemplate.convertAndSend(
                "/topic/conversations/" + chatMessage.getConversationId(), 
                chatMessage);
            
            // Save message to database (asynchronously)
            try {
                MessageRequest request = new MessageRequest();
                request.setContent(chatMessage.getContent());
                request.setContentType(com.example.chatservice.model.Message.MessageType.TEXT);
                
                chatService.sendMessage(
                    chatMessage.getConversationId(), 
                    chatMessage.getSender(), 
                    request);
            } catch (Exception e) {
                logger.error("Error saving WebSocket message to database: {}", e.getMessage());
            }
            
            // We don't want to broadcast private messages to public topic
            return null;
        }
        
        // Return the message to broadcast to public topic
        return chatMessage;
    }
    
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, 
                               SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            logger.error("No authenticated principal found for user join");
            return null;
        }
        
        String authenticatedUsername = principal.getName();
        logger.info("Authenticated user joining chat: {}", authenticatedUsername);
        
        if (!authenticatedUsername.equals(chatMessage.getSender())) {
            logger.error("Username mismatch when joining chat. Token: {}, Message: {}", 
                authenticatedUsername, chatMessage.getSender());
            return null;
        }
        
        chatMessage.setTimestamp(LocalDateTime.now().format(formatter));
        chatMessage.setType(ChatMessage.MessageType.JOIN);
        chatMessage.setContent(chatMessage.getSender() + " joined the chat");
        
        // Log join message to terminal
        logger.info("USER JOINED: {} - {}", 
            chatMessage.getTimestamp(), 
            chatMessage.getSender());
        
        // Also print to console for direct terminal visibility
        System.out.println("USER JOINED: " + chatMessage.getTimestamp() + 
            " - " + chatMessage.getSender());
        
        // Add username in websocket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        
        // Check if this is for a specific conversation
        if (chatMessage.getConversationId() != null) {
            // Send join notification to specific conversation
            ChatMessage conversationJoinMessage = new ChatMessage();
            conversationJoinMessage.setType(ChatMessage.MessageType.JOIN);
            conversationJoinMessage.setSender(chatMessage.getSender());
            conversationJoinMessage.setTimestamp(chatMessage.getTimestamp());
            conversationJoinMessage.setContent(chatMessage.getSender() + " joined the conversation");
            conversationJoinMessage.setConversationId(chatMessage.getConversationId());
            
            messagingTemplate.convertAndSend(
                "/topic/conversations/" + chatMessage.getConversationId(), 
                conversationJoinMessage);
            
            // We don't want to broadcast private conversation joins to public
            return null;
        }
        
        return chatMessage;
    }
    
    @MessageMapping("/chat.private")
    public void privateMessage(@Payload ChatMessage chatMessage,
                              SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            logger.error("No authenticated principal found for private message");
            return;
        }
        
        String authenticatedUsername = principal.getName();
        logger.info("Authenticated user sending private message: {}", authenticatedUsername);
        
        if (!authenticatedUsername.equals(chatMessage.getSender())) {
            logger.error("Username mismatch in private message. Token: {}, Message: {}", 
                authenticatedUsername, chatMessage.getSender());
            return;
        }
        
        String sender = chatMessage.getSender();
        String recipient = chatMessage.getRecipient();
        
        if (recipient == null || recipient.isEmpty()) {
            logger.error("Private message missing recipient: {}", chatMessage);
            return;
        }
        
        chatMessage.setTimestamp(LocalDateTime.now().format(formatter));
        // Set the message type to PRIVATE explicitly
        chatMessage.setType(ChatMessage.MessageType.PRIVATE);
        
        try {
            // Create or get existing conversation
            DirectConversationRequest request = new DirectConversationRequest();
            request.setRecipientId(recipient);
            
            // Create or retrieve conversation
            ConversationResponse conversation = chatService.createDirectConversation(sender, request);
            String conversationId = conversation.getId();
            
            // Update the message with conversation ID
            chatMessage.setConversationId(conversationId);
            
            // Save the message to database
            MessageRequest messageRequest = new MessageRequest();
            messageRequest.setContent(chatMessage.getContent());
            messageRequest.setContentType(com.example.chatservice.model.Message.MessageType.TEXT);
            
            chatService.sendMessage(conversationId, sender, messageRequest);
            
            // Send the message to the conversation topic so both users can receive it
            logger.info("### SENDING MESSAGE TO TOPIC: /topic/conversations/{} ###", conversationId);
            logger.info("### MESSAGE CONTENT: {} ###", chatMessage.getContent());
            logger.info("### SENDER: {} ###", chatMessage.getSender());
            logger.info("### RECIPIENT: {} ###", chatMessage.getRecipient());
            logger.info("### MESSAGE TYPE: {} ###", chatMessage.getType());
            logger.info("### CONVERSATION ID: {} ###", chatMessage.getConversationId());
            
            messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, chatMessage);
            
            logger.info("### MESSAGE SENT SUCCESSFULLY TO TOPIC ###");
            logger.info("Private message sent from {} to {} in conversation {}", 
                sender, recipient, conversationId);
        } catch (Exception e) {
            logger.error("Error processing private message: {}", e.getMessage(), e);
            
            // Send error back to sender
            Map<String, Object> errorMessage = new HashMap<>();
            errorMessage.put("error", "Failed to send private message");
            errorMessage.put("reason", e.getMessage());
            
            messagingTemplate.convertAndSendToUser(
                sender, 
                "/queue/errors", 
                errorMessage
            );
        }
    }
} 