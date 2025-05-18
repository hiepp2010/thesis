package com.example.chatservice.repository;

import com.example.chatservice.model.DirectConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DirectConversationRepository extends JpaRepository<DirectConversation, String> {
    
    @Query("SELECT d FROM DirectConversation d WHERE d.user1Id = ?1 OR d.user2Id = ?1")
    List<DirectConversation> findByUserId(String userId);
    
    @Query("SELECT d FROM DirectConversation d WHERE (d.user1Id = ?1 AND d.user2Id = ?2) OR (d.user1Id = ?2 AND d.user2Id = ?1)")
    Optional<DirectConversation> findByUser1IdAndUser2Id(String user1Id, String user2Id);
} 