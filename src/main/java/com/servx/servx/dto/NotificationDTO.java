package com.servx.servx.dto;

import com.servx.servx.entity.NotificationPayload;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class NotificationDTO {

    private Long id;
    private NotificationType type;

    private LocalDateTime createdAt;

    private boolean isRead;

    private NotificationPayload payload;

    public enum NotificationType {
        NEW_REQUEST,
        REQUEST_ACCEPTED,
        REQUEST_DECLINED,
        BOOKING_CONFIRMED,
        SERVICE_COMPLETED,
        SYSTEM_ALERT,
        BOOKING_CANCELLED,
        PROVIDER_MARKED_COMPLETE,
        SEEKER_CONFIRMED_COMPLETION
    }
}
