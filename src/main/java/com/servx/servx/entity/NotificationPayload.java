package com.servx.servx.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPayload {
    private Long serviceRequestId;
    private Long bookingId;
    private String message;
    private Long userId;
}