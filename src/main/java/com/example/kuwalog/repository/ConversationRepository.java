package com.example.kuwalog.repository;

import com.example.kuwalog.entity.Conversation;
import com.example.kuwalog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c JOIN FETCH c.beetle JOIN FETCH c.initiator JOIN FETCH c.owner " +
           "WHERE c.initiator.id = :userId OR c.owner.id = :userId ORDER BY c.createdAt DESC")
    List<Conversation> findByParticipantIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT c FROM Conversation c JOIN FETCH c.beetle JOIN FETCH c.initiator JOIN FETCH c.owner WHERE c.id = :id")
    Optional<Conversation> findByIdWithAll(@Param("id") Long id);

    Optional<Conversation> findByBeetleIdAndInitiatorId(Long beetleId, Long initiatorId);

    // 生体のオーナーが問い合わせを受けた相手（initiator）一覧
    @Query("SELECT c.initiator FROM Conversation c WHERE c.beetle.id = :beetleId AND c.owner.id = :ownerId")
    List<User> findInitiatorsByBeetleIdAndOwnerId(@Param("beetleId") Long beetleId, @Param("ownerId") Long ownerId);
}
