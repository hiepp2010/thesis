package com.example.chatservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "direct_conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectConversation {
    
    @Id
    private String conversationId;
    
    @OneToOne
    @JoinColumn(name = "conversation_id")
    @MapsId
    private Conversation conversation;
    
    private String user1Id;
    
    private String user2Id;
} 