package com.servx.servx.entity;

import com.servx.servx.util.NotificationPayloadConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(columnDefinition = "jsonb")
    @Convert(converter = NotificationPayloadConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    private NotificationPayload payload;


    @Builder.Default
    @Column(nullable = false)
    private boolean isRead = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum NotificationType {
        NEW_REQUEST, REQUEST_ACCEPTED, REQUEST_DECLINED,
        BOOKING_CONFIRMED, SERVICE_COMPLETED, SYSTEM_ALERT,BOOKING_CANCELLED
    }
}