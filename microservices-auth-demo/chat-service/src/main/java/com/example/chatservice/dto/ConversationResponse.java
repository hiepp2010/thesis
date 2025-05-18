package com.example.chatservice.dto;

import com.example.chatservice.model.Conversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private String id;
    private Conversation.ConversationType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private MessageResponse lastMessage;
    private String otherUserId; // For direct conversations only
} 