package com.servx.servx.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servx.servx.dto.BookingRequestPayload;
import com.servx.servx.dto.ChatConversationDTO;
import com.servx.servx.dto.ChatMessageDTO;
import com.servx.servx.entity.ChatMessage;
import com.servx.servx.entity.ServiceRequest;
import com.servx.servx.entity.User;
import com.servx.servx.exception.UnauthorizedAccessException;
import com.servx.servx.exception.UserNotFoundException;
import com.servx.servx.repository.ChatMessageRepository;
import com.servx.servx.repository.ServiceRequestRepository;
import com.servx.servx.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper; // Inject ObjectMapper

    @Transactional
    public ChatMessageDTO saveMessage(ChatMessageDTO dto, String senderEmail) {

        log.info("Saving message with senderEmail: {}", senderEmail);

        User sender = userRepository.findByEmailIgnoreCase(senderEmail)
                .orElseThrow(() -> new UserNotFoundException("Sender not found: " + senderEmail));
        ServiceRequest request = serviceRequestRepository.findById(dto.getServiceRequestId())
                .orElseThrow(() -> new EntityNotFoundException("ServiceRequest not found: " + dto.getServiceRequestId()));

        log.info("Fetched sender: ID={}, Email={}", sender.getId(), sender.getEmail());

        if (!isChatActive(request.getStatus())) {
            log.warn("Attempted to send message for inactive request {}. Status: {}", request.getId(), request.getStatus());
            throw new IllegalStateException("Chat is not active for this service request (Status: " + request.getStatus() + ")");
        }

        User recipient;
        if (request.getSeeker().getId().equals(sender.getId())) {
            recipient = request.getProvider();
        } else if (request.getProvider().getId().equals(sender.getId())) {
            recipient = request.getSeeker();
        } else {
            throw new UnauthorizedAccessException("Sender is not part of this service request chat");
        }

        if (dto.getRecipientId() == null || !recipient.getId().equals(dto.getRecipientId())) {
            log.warn("Recipient ID mismatch or missing in DTO. Derived: {}, DTO: {}", recipient.getId(), dto.getRecipientId());
            throw new IllegalArgumentException("Recipient ID in DTO does not match or is missing.");
        }

        ChatMessage message = ChatMessage.builder()
                .serviceRequest(request)
                .sender(sender)
                .recipient(recipient)
                .content(dto.getContent())
                .timestamp(Instant.now())
                .isRead(false)
                .build();

        if (dto.getBookingPayload() != null) {
            try {
                String payloadJson = objectMapper.writeValueAsString(dto.getBookingPayload());
                message.setBookingPayloadJson(payloadJson);
                print("Serialized booking payload for message");
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize BookingRequestPayload: {}", e.getMessage());
                throw new RuntimeException("Failed to process booking payload", e);
            }
        }

        ChatMessage savedMessage = chatMessageRepository.save(message);
        print("Message saved with ID: " + savedMessage.getId());

        return mapToDto(savedMessage);
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageDTO> getMessagesForRequest(Long requestId, Long userId, Pageable pageable) {
        validateUserAccessToRequest(requestId, userId);
        Page<ChatMessage> messagePage = chatMessageRepository.findByServiceRequestIdOrderByTimestampDesc(requestId, pageable);
        return messagePage.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<ChatConversationDTO> getUserConversations(Long userId) {
        log.info("Fetching conversations for user ID: {}", userId);

        List<ServiceRequest> seekerRequests = serviceRequestRepository.findBySeekerIdOrderByCreatedAtDesc(userId);
        log.debug("Found {} requests as seeker for user {}", seekerRequests.size(), userId);

        List<ServiceRequest> providerRequests = serviceRequestRepository.findByProviderIdOrderByCreatedAtDesc(userId);
        log.debug("Found {} requests as provider for user {}", providerRequests.size(), userId);

        Map<Long, ServiceRequest> combinedRequestsMap = new HashMap<>();
        seekerRequests.forEach(req -> combinedRequestsMap.putIfAbsent(req.getId(), req));
        providerRequests.forEach(req -> combinedRequestsMap.putIfAbsent(req.getId(), req));
        List<ServiceRequest> allUserRequests = new ArrayList<>(combinedRequestsMap.values());
        log.debug("Total unique requests for user {}: {}", userId, allUserRequests.size());

        return allUserRequests.stream()
                .map(request -> {
                    User otherParticipant = request.getSeeker().getId().equals(userId)
                            ? request.getProvider()
                            : request.getSeeker();
                    Optional<ChatMessage> lastMessageOpt = chatMessageRepository.findTopByServiceRequestIdOrderByTimestampDesc(request.getId());
                    long unreadCount = chatMessageRepository.countByServiceRequestIdAndRecipientIdAndIsReadFalse(request.getId(), userId);

                    Instant timestampForDto = lastMessageOpt
                            .map(ChatMessage::getTimestamp)
                            .orElseGet(() -> request.getCreatedAt() != null ? request.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : Instant.MIN);

                    return ChatConversationDTO.builder()
                            .serviceRequestId(request.getId())
                            .otherParticipantName(otherParticipant.getFirstName() + " " + otherParticipant.getLastName())
                            .otherParticipantId(otherParticipant.getId())
                            .lastMessage(lastMessageOpt.map(ChatMessage::getContent).orElse("No messages yet"))
                            .lastMessageTimestamp(timestampForDto)
                            .unreadCount(unreadCount)
                            .requestStatus(request.getStatus())
                            .build();
                })
                .sorted(Comparator.comparing(
                                ChatConversationDTO::getLastMessageTimestamp,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        ).reversed()
                )
                .collect(Collectors.toList());
    }

    @Transactional
    public void markMessagesAsRead(Long requestId, Long userId) {
        validateUserAccessToRequest(requestId, userId);

        int updatedCount = chatMessageRepository.markMessagesAsRead(requestId, userId);
        log.info("Marked {} messages as read for user {} in request {}", updatedCount, userId, requestId);

    }

    public ChatMessageDTO mapToDto(ChatMessage message) {
        ChatMessageDTO.ChatMessageDTOBuilder dtoBuilder = ChatMessageDTO.builder()
                .id(message.getId())
                .serviceRequestId(message.getServiceRequest().getId())
                .senderId(message.getSender().getId())
                .recipientId(message.getRecipient().getId())
                .senderName(message.getSender().getFirstName())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .isRead(message.isRead())
                .recipientEmail(message.getRecipient().getEmail()); // Add recipient email

        if (StringUtils.hasText(message.getBookingPayloadJson())) {
            try {
                BookingRequestPayload payload = objectMapper.readValue(
                        message.getBookingPayloadJson(),
                        BookingRequestPayload.class
                );
                dtoBuilder.bookingPayload(payload);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize bookingPayloadJson for message ID {}: {}", message.getId(), e.getMessage());
            }
        }

        return dtoBuilder.build();
    }

    private void validateUserAccessToRequest(Long requestId, Long userId) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("ServiceRequest not found: " + requestId));
        if (!request.getSeeker().getId().equals(userId) && !request.getProvider().getId().equals(userId)) {
            throw new UnauthorizedAccessException("User " + userId + " does not have access to request " + requestId);
        }
    }

    private boolean isChatActive(ServiceRequest.RequestStatus status) {
        // Use the RequestStatus enum defined in ServiceRequest entity
        return status == ServiceRequest.RequestStatus.ACCEPTED
                || status == ServiceRequest.RequestStatus.BOOKING_CONFIRMED;
    }

    // Print helper (if needed, otherwise remove)
    private void print(String message) {
        System.out.println(message);
    }
}