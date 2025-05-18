package com.example.chatservice.service;

import com.example.chatservice.dto.ConversationResponse;
import com.example.chatservice.dto.DirectConversationRequest;
import com.example.chatservice.dto.MessageRequest;
import com.example.chatservice.dto.MessageResponse;
import com.example.chatservice.model.Conversation;
import com.example.chatservice.model.DirectConversation;
import com.example.chatservice.model.Message;
import com.example.chatservice.repository.ConversationRepository;
import com.example.chatservice.repository.DirectConversationRepository;
import com.example.chatservice.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final DirectConversationRepository directConversationRepository;
    private final MessageRepository messageRepository;

    @Override
    @Transactional
    public ConversationResponse createDirectConversation(String userId, DirectConversationRequest request) {
        // Check if conversation already exists
        Optional<DirectConversation> existingConversation = 
                directConversationRepository.findByUser1IdAndUser2Id(userId, request.getRecipientId());
        
        if (existingConversation.isPresent()) {
            return mapToConversationResponse(existingConversation.get().getConversation(), userId);
        }
        
        // Create new conversation
        Conversation conversation = Conversation.builder()
                .type(Conversation.ConversationType.DIRECT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        conversation = conversationRepository.save(conversation);
        
        DirectConversation directConversation = new DirectConversation();
        directConversation.setConversation(conversation);
        directConversation.setUser1Id(userId);
        directConversation.setUser2Id(request.getRecipientId());
        
        directConversationRepository.save(directConversation);
        
        return mapToConversationResponse(conversation, userId);
    }

    @Override
    public List<ConversationResponse> getUserConversations(String userId) {
        List<DirectConversation> directConversations = directConversationRepository.findByUserId(userId);
        
        return directConversations.stream()
                .map(dc -> mapToConversationResponse(dc.getConversation(), userId))
                .collect(Collectors.toList());
    }

    @Override
    public Page<MessageResponse> getConversationMessages(String conversationId, String userId, Pageable pageable) {
        // Check if user has access to the conversation
        if (!userHasAccessToConversation(userId, conversationId)) {
            throw new RuntimeException("User does not have access to this conversation");
        }
        
        return messageRepository.findByConversationIdAndParentMessageIsNullOrderBySentAtDesc(conversationId, pageable)
                .map(this::mapToMessageResponse);
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(String conversationId, String userId, MessageRequest request) {
        // Check if user has access to the conversation
        if (!userHasAccessToConversation(userId, conversationId)) {
            throw new RuntimeException("User does not have access to this conversation");
        }
        
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        Message message = Message.builder()
                .conversation(conversation)
                .senderId(userId)
                .content(request.getContent())
                .contentType(request.getContentType())
                .sentAt(LocalDateTime.now())
                .edited(false)
                .build();
        
        message = messageRepository.save(message);
        
        // Update conversation with last message
        conversation.setLastMessage(message);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        
        return mapToMessageResponse(message);
    }

    @Override
    @Transactional
    public MessageResponse updateMessage(String messageId, String userId, MessageRequest request) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // Check if user is the sender
        if (!message.getSenderId().equals(userId)) {
            throw new RuntimeException("User is not the sender of this message");
        }
        
        message.setContent(request.getContent());
        message.setContentType(request.getContentType());
        message.setEdited(true);
        
        message = messageRepository.save(message);
        
        return mapToMessageResponse(message);
    }

    @Override
    @Transactional
    public void deleteMessage(String messageId, String userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // Check if user is the sender
        if (!message.getSenderId().equals(userId)) {
            throw new RuntimeException("User is not the sender of this message");
        }
        
        messageRepository.delete(message);
    }
    
    private boolean userHasAccessToConversation(String userId, String conversationId) {
        // For direct conversations, check if user is one of the participants
        Optional<DirectConversation> directConversation = directConversationRepository.findById(conversationId);
        
        if (directConversation.isPresent()) {
            DirectConversation dc = directConversation.get();
            return dc.getUser1Id().equals(userId) || dc.getUser2Id().equals(userId);
        }
        
        return false;
    }
    
    private MessageResponse mapToMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .contentType(message.getContentType())
                .sentAt(message.getSentAt())
                .edited(message.isEdited())
                .build();
    }
    
    private ConversationResponse mapToConversationResponse(Conversation conversation, String userId) {
        MessageResponse lastMessageResponse = null;
        if (conversation.getLastMessage() != null) {
            lastMessageResponse = mapToMessageResponse(conversation.getLastMessage());
        }
        
        ConversationResponse.ConversationResponseBuilder builder = ConversationResponse.builder()
                .id(conversation.getId())
                .type(conversation.getType())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .lastMessage(lastMessageResponse);
        
        // For direct conversations, add the other user ID
        if (conversation.getType() == Conversation.ConversationType.DIRECT) {
            Optional<DirectConversation> directConversation = directConversationRepository.findById(conversation.getId());
            if (directConversation.isPresent()) {
                DirectConversation dc = directConversation.get();
                String otherUserId = dc.getUser1Id().equals(userId) ? dc.getUser2Id() : dc.getUser1Id();
                builder.otherUserId(otherUserId);
            }
        }
        
        return builder.build();
    }
} 