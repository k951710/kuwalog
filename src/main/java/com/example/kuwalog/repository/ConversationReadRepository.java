package com.example.kuwalog.repository;

import com.example.kuwalog.entity.ConversationRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversationReadRepository extends JpaRepository<ConversationRead, Long> {

    Optional<ConversationRead> findByConversationIdAndUserId(Long conversationId, Long userId);

    @Query("SELECT COUNT(DISTINCT m.conversation.id) FROM Message m " +
           "LEFT JOIN ConversationRead cr ON cr.conversation = m.conversation AND cr.user.id = :userId " +
           "WHERE m.sender.id != :userId " +
           "AND (m.conversation.initiator.id = :userId OR m.conversation.owner.id = :userId) " +
           "AND (cr.lastReadAt IS NULL OR m.createdAt > cr.lastReadAt)")
    long countUnreadConversations(@Param("userId") Long userId);
}
