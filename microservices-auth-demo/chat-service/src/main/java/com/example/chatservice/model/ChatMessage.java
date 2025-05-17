package com.example.chatservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String sender;
    private String content;
    private String timestamp;
    private MessageType type;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }
} 