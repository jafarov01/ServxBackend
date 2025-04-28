package com.servx.servx.dto;

import com.servx.servx.entity.ServiceRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationDTO {
    private Long serviceRequestId;
    private String otherParticipantName;
    private Long otherParticipantId;
    private String lastMessage;
    private Instant lastMessageTimestamp;
    private long unreadCount;
    private String otherParticipantPhotoUrl;
    private ServiceRequest.RequestStatus requestStatus;
}
