package com.example.chatservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;
    
    @ManyToOne
    @JoinColumn(name = "parent_message_id")
    private Message parentMessage;
    
    private String senderId;
    
    private String content;
    
    @Enumerated(EnumType.STRING)
    private MessageType contentType;
    
    private LocalDateTime sentAt;
    
    private boolean edited;
    
    public enum MessageType {
        TEXT, IMAGE, FILE
    }
} 