package com.example.chatservice.dto;

import com.example.chatservice.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String id;
    private String conversationId;
    private String senderId;
    private String content;
    private Message.MessageType contentType;
    private LocalDateTime sentAt;
    private boolean edited;
} 