package com.example.chatservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Enumerated(EnumType.STRING)
    private ConversationType type;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @OneToOne
    @JoinColumn(name = "last_message_id")
    private Message lastMessage;
    
    public enum ConversationType {
        DIRECT, GROUP
    }
} 