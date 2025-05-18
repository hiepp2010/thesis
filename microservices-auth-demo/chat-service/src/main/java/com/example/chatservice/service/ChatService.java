package com.example.chatservice.service;

import com.example.chatservice.dto.ConversationResponse;
import com.example.chatservice.dto.DirectConversationRequest;
import com.example.chatservice.dto.MessageRequest;
import com.example.chatservice.dto.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatService {
    
    ConversationResponse createDirectConversation(String userId, DirectConversationRequest request);
    
    List<ConversationResponse> getUserConversations(String userId);
    
    Page<MessageResponse> getConversationMessages(String conversationId, String userId, Pageable pageable);
    
    MessageResponse sendMessage(String conversationId, String userId, MessageRequest request);
    
    MessageResponse updateMessage(String messageId, String userId, MessageRequest request);
    
    void deleteMessage(String messageId, String userId);
} 