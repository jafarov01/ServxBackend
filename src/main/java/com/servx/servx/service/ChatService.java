package com.servx.servx.service;

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

    // Saves a new message received via WebSocket
    // Inherits class-level @Transactional (read-write)
// Inside ChatService.java

    // Change return type from ChatMessage to ChatMessageDTO
    @Transactional
    public ChatMessageDTO saveMessage(ChatMessageDTO dto, String senderEmail) {
        User sender = userRepository.findByEmailIgnoreCase(senderEmail)
                .orElseThrow(() -> new UserNotFoundException("Sender not found: " + senderEmail));
        ServiceRequest request = serviceRequestRepository.findById(dto.getServiceRequestId())
                .orElseThrow(() -> new EntityNotFoundException("ServiceRequest not found: " + dto.getServiceRequestId()));

        // --- Purpose-Oriented Chat Check ---
        if (!isChatActive(request.getStatus())) {
            log.warn("Attempted to send message for inactive request {}. Status: {}", request.getId(), request.getStatus());
            throw new IllegalStateException("Chat is not active for this service request (Status: " + request.getStatus() + ")");
        }

        // --- Determine Recipient (Using ID comparison - keep this fix) ---
        User recipient;
        if (request.getSeeker().getId().equals(sender.getId())) {
            recipient = request.getProvider();
        } else if (request.getProvider().getId().equals(sender.getId())) {
            recipient = request.getSeeker();
        } else {
            throw new UnauthorizedAccessException("Sender is not part of this service request chat");
        }

        // --- Validation (Keep this fix) ---
        if (dto.getRecipientId() == null || !recipient.getId().equals(dto.getRecipientId())) {
            log.warn("Recipient ID mismatch or missing in DTO. Derived: {}, DTO: {}", recipient.getId(), dto.getRecipientId());
            throw new IllegalArgumentException("Recipient ID in DTO does not match or is missing.");
        }

        // --- Build Entity ---
        ChatMessage message = ChatMessage.builder()
                .serviceRequest(request)
                .sender(sender)
                .recipient(recipient)
                .content(dto.getContent())
                .timestamp(Instant.now()) // Use Instant on backend
                .isRead(false)
                .build();

        // --- Save Entity ---
        ChatMessage savedMessage = chatMessageRepository.save(message);
        log.info("Message saved with ID: {}", savedMessage.getId());

        // --- Map to DTO and return ---
        // Calling mapToDto HERE ensures lazy loading happens within the transaction
        return mapToDto(savedMessage); // Return DTO instead of Entity
    }

    // Fetches historical messages for a specific request
    // Overrides class-level @Transactional with readOnly=true
    @Transactional(readOnly = true) // Ensure this import exists: org.springframework.transaction.annotation.Transactional
    public Page<ChatMessageDTO> getMessagesForRequest(Long requestId, Long userId, Pageable pageable) {
        validateUserAccessToRequest(requestId, userId);
        Page<ChatMessage> messagePage = chatMessageRepository.findByServiceRequestIdOrderByTimestampDesc(requestId, pageable);
        return messagePage.map(this::mapToDto);
    }

    // Fetches conversation list for the user's inbox
    @Transactional(readOnly = true) // Override for read-only transaction
    public List<ChatConversationDTO> getUserConversations(Long userId) {
        log.info("Fetching conversations for user ID: {}", userId);

        // 1. Fetch all service requests where the user is the seeker
        List<ServiceRequest> seekerRequests = serviceRequestRepository.findBySeekerIdOrderByCreatedAtDesc(userId);
        log.debug("Found {} requests as seeker for user {}", seekerRequests.size(), userId);

        // 2. Fetch all service requests where the user is the provider
        List<ServiceRequest> providerRequests = serviceRequestRepository.findByProviderIdOrderByCreatedAtDesc(userId);
        log.debug("Found {} requests as provider for user {}", providerRequests.size(), userId);

        // 3. Combine the lists, ensuring uniqueness based on request ID
        Map<Long, ServiceRequest> combinedRequestsMap = new HashMap<>();
        seekerRequests.forEach(req -> combinedRequestsMap.putIfAbsent(req.getId(), req));
        providerRequests.forEach(req -> combinedRequestsMap.putIfAbsent(req.getId(), req));
        List<ServiceRequest> allUserRequests = new ArrayList<>(combinedRequestsMap.values());
        log.debug("Total unique requests for user {}: {}", userId, allUserRequests.size());


        // 4. Map each relevant ServiceRequest to a ChatConversationDTO
        return allUserRequests.stream()
                .map(request -> {
                    // Determine the other participant in the conversation
                    User otherParticipant = request.getSeeker().getId().equals(userId)
                            ? request.getProvider()
                            : request.getSeeker();

                    // Find the latest message for this request (if any)
                    Optional<ChatMessage> lastMessageOpt = chatMessageRepository.findTopByServiceRequestIdOrderByTimestampDesc(request.getId());

                    // Count unread messages sent TO the current user in this request
                    long unreadCount = chatMessageRepository.countByServiceRequestIdAndRecipientIdAndIsReadFalse(request.getId(), userId);

                    // Determine the timestamp for sorting/display (use last message time or fallback)
                    // Fallback uses the request creation time, converted to Instant
                    Instant timestampForDto = lastMessageOpt
                            .map(ChatMessage::getTimestamp) // Get Instant from last message
                            .orElseGet(() -> // Otherwise, convert request creation time (LocalDateTime)
                                    request.getCreatedAt() != null
                                            ? request.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() // Use system zone
                                            : Instant.MIN // Sensible default if createdAt is somehow null
                            );

                    // Build the DTO
                    return ChatConversationDTO.builder()
                            .serviceRequestId(request.getId())
                            .otherParticipantName(otherParticipant.getFirstName() + " " + otherParticipant.getLastName())
                            .otherParticipantId(otherParticipant.getId())
                            .lastMessage(lastMessageOpt.map(ChatMessage::getContent).orElse("No messages yet"))
                            .lastMessageTimestamp(timestampForDto) // Use the determined Instant
                            .unreadCount(unreadCount)
                            .requestStatus(request.getStatus())
                            .build();
                })
                // 5. Sort the list of DTOs by the timestamp (newest first)
                .sorted(Comparator.comparing(
                                ChatConversationDTO::getLastMessageTimestamp, // Compare using the Instant field
                                Comparator.nullsLast(Comparator.naturalOrder()) // Handle potential nulls (safety check)
                        ).reversed() // Newest first
                )
                // 6. Collect the results into a List
                .collect(Collectors.toList());
    }

    // Marks messages in a conversation as read by the specified user
    // Inherits class-level @Transactional (read-write)
    public void markMessagesAsRead(Long requestId, Long userId) {
        validateUserAccessToRequest(requestId, userId);

        // Find all messages in this request sent TO this user that are currently unread
        List<ChatMessage> unreadMessages = chatMessageRepository.findAll((root, query, cb) ->
                cb.and(
                        cb.equal(root.get("serviceRequest").get("id"), requestId),
                        cb.equal(root.get("recipient").get("id"), userId),
                        cb.isFalse(root.get("isRead"))
                )
        );

        if (!unreadMessages.isEmpty()) {
            log.info("Marking {} messages as read for user {} in request {}", unreadMessages.size(), userId, requestId);
            // Update the isRead flag for each message
            unreadMessages.forEach(msg -> msg.setRead(true));
            // Save all updated messages in batch
            chatMessageRepository.saveAll(unreadMessages);
        }
    }

    // Helper to map ChatMessage Entity to ChatMessageDTO
    public ChatMessageDTO mapToDto(ChatMessage message) {
        // Ensure lazy-loaded fields are accessible if needed, or fetch eagerly
        // For names, it seems User entity is already loaded.
        return ChatMessageDTO.builder()
                .id(message.getId())
                .serviceRequestId(message.getServiceRequest().getId())
                .senderId(message.getSender().getId())
                .recipientId(message.getRecipient().getId())
                .senderName(message.getSender().getFirstName()) // Assumes User has getFirstName()
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .isRead(message.isRead())
                .build();
    }

    // Helper to check if user is part of the request (seeker or provider)
    private void validateUserAccessToRequest(Long requestId, Long userId) {
        // Fetch the request including seeker and provider details efficiently if possible
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("ServiceRequest not found: " + requestId));
        // Check if the user ID matches either the seeker's or provider's ID
        if (!request.getSeeker().getId().equals(userId) && !request.getProvider().getId().equals(userId)) {
            throw new UnauthorizedAccessException("User " + userId + " does not have access to request " + requestId);
        }
    }

    // Helper to determine if chat should be active based on request status
    private boolean isChatActive(ServiceRequest.RequestStatus status) {
        // Only allow chat during specific phases of the service request
        return status == ServiceRequest.RequestStatus.ACCEPTED
                || status == ServiceRequest.RequestStatus.BOOKING_CONFIRMED; // Adjust as needed
    }
}