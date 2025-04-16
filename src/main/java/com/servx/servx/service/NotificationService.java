package com.servx.servx.service;

import com.servx.servx.dto.NotificationDTO;
import com.servx.servx.entity.Notification;
import com.servx.servx.entity.NotificationPayload;
import com.servx.servx.entity.User;
import com.servx.servx.repository.NotificationRepository;
import com.servx.servx.util.NotificationMapper;
import org.springframework.data.domain.Pageable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public void createNotification(User recipient, Notification.NotificationType type, NotificationPayload payload) {

        Notification notification = Notification.builder()
                .type(type)
                .recipient(recipient)
                .payload(payload)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    public List<NotificationDTO> getUserNotifications(User user, boolean unreadOnly) {
        Specification<Notification> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("recipient"), user));
            if (unreadOnly) {
                predicates.add(cb.isFalse(root.get("isRead")));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return notificationRepository.findAll(spec).stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
    }

    public void markNotificationAsRead(Long notificationId) {
        notificationRepository.findById(notificationId)
                .ifPresent(notification -> notification.setRead(true));
    }
}