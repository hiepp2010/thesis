package com.example.chatservice.repository;

import com.example.chatservice.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    
    Page<Message> findByConversationIdAndParentMessageIsNullOrderBySentAtDesc(String conversationId, Pageable pageable);
    
    Page<Message> findByParentMessageIdOrderBySentAtDesc(String parentMessageId, Pageable pageable);
} 