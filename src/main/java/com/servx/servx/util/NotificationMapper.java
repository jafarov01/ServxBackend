package com.servx.servx.util;

import com.servx.servx.dto.NotificationDTO;
import com.servx.servx.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationDTO toDto(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .type(mapType(notification.getType()))
                .createdAt(notification.getCreatedAt())
                .isRead(notification.isRead())
                .payload(notification.getPayload())
                .build();
    }

    private NotificationDTO.NotificationType mapType(Notification.NotificationType type) {
        return NotificationDTO.NotificationType.valueOf(type.name());
    }
}
