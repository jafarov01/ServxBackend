package com.servx.servx.repository;

import com.servx.servx.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>, JpaSpecificationExecutor<ChatMessage> {

    // Find messages for a specific service request, ordered by time
    List<ChatMessage> findByServiceRequestIdOrderByTimestampAsc(Long serviceRequestId);

    // Find messages for a specific service request with pagination
    Page<ChatMessage> findByServiceRequestIdOrderByTimestampDesc(Long serviceRequestId, Pageable pageable);

    // Find the latest message for a service request
    Optional<ChatMessage> findTopByServiceRequestIdOrderByTimestampDesc(Long serviceRequestId);

    // Count unread messages for a user in a specific request
    long countByServiceRequestIdAndRecipientIdAndIsReadFalse(Long serviceRequestId, Long recipientId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true " +
            "WHERE m.serviceRequest.id = :requestId " +
            "AND m.recipient.id = :userId " +
            "AND m.isRead = false")
    int markMessagesAsRead(@Param("requestId") Long requestId, @Param("userId") Long userId);
}