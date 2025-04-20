package com.servx.servx.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private Long serviceRequestId;
    private Long senderId;
    private Long recipientId; // Client needs to know who receives it
    private String senderName; // Helpful for UI display
    private String content;
    private Instant timestamp;
    private boolean isRead; // Include read status
}