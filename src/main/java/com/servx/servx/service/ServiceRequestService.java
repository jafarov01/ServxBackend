package com.servx.servx.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servx.servx.dto.*;
import com.servx.servx.entity.*;
import com.servx.servx.exception.UnauthorizedAccessException;
import com.servx.servx.repository.ChatMessageRepository;
import com.servx.servx.repository.ServiceProfileRepository;
import com.servx.servx.repository.ServiceRequestRepository;
import com.servx.servx.util.ServiceRequestMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceRequestService {
    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceProfileRepository serviceProfileRepository;
    private final ChatMessageRepository chatMessageRepository; // Inject Chat repo
    private final BookingService bookingService; // Inject Booking service
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final ServiceRequestMapper serviceRequestMapper;

    @Transactional
    public ServiceRequestResponseDTO createServiceRequest(ServiceRequestDTO dto, User authenticatedUser) {
        ServiceProfile service = serviceProfileRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new EntityNotFoundException("Service not found with id: " + dto.getServiceId()));

        User provider = service.getUser();

        ServiceRequest request = ServiceRequest.builder()
                .description(dto.getDescription())
                .severity(dto.getSeverity())
                .address(serviceRequestMapper.mapAddress(dto.getAddress()))
                .service(service)
                .seeker(authenticatedUser)
                .provider(provider)
                .status(ServiceRequest.RequestStatus.PENDING)
                .build();

        ServiceRequest savedRequest = serviceRequestRepository.save(request);
        notificationService.createNotification(
                provider,
                Notification.NotificationType.NEW_REQUEST,
                new NotificationPayload(
                        savedRequest.getId(),
                        null,  // bookingId if available
                        "New service request from " + (authenticatedUser.getFirstName() + " " + authenticatedUser.getLastName()),
                        authenticatedUser.getId()
                )
        );
        return serviceRequestMapper.toDto(savedRequest); // Using ServiceRequestMapper
    }

    @Transactional
    public ServiceRequestResponseDTO acceptRequest(Long requestId, User provider) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));

        if (!request.getProvider().equals(provider)) {
            throw new UnauthorizedAccessException("Only the provider can accept requests");
        }

        request.setStatus(ServiceRequest.RequestStatus.ACCEPTED);
        ServiceRequest updated = serviceRequestRepository.save(request);

        // Create notification for seeker
        notificationService.createNotification(
                request.getSeeker(),
                Notification.NotificationType.REQUEST_ACCEPTED,
                new NotificationPayload(
                        request.getId(),
                        null,
                        "Your service request has been accepted",
                        provider.getId()
                )
        );

        return serviceRequestMapper.toDto(updated);
    }

    @Transactional
    public ServiceRequestResponseDTO confirmBooking(Long requestId, Long messageId, User seeker) {
        log.info("Attempting to confirm booking for request ID {} from message ID {} by seeker ID {}", requestId, messageId, seeker.getId());
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Service Request not found with ID: " + requestId));

        if (!request.getSeeker().getId().equals(seeker.getId())) {
            throw new UnauthorizedAccessException("Only the service seeker can confirm the booking.");
        }
        if (request.getStatus() != ServiceRequest.RequestStatus.ACCEPTED) {
            throw new IllegalStateException("Booking can only be confirmed if the request is currently accepted.");
        }

        ChatMessage bookingMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Booking proposal chat message not found: " + messageId));

        if (!bookingMessage.getServiceRequest().getId().equals(requestId)) {
            throw new IllegalArgumentException("Message " + messageId + " does not belong to request " + requestId);
        }
        if (!StringUtils.hasText(bookingMessage.getBookingPayloadJson())) {
            throw new IllegalStateException("Cannot confirm booking: Message " + messageId + " does not contain booking payload.");
        }

        BookingRequestPayload payload;
        try {
            payload = objectMapper.readValue(bookingMessage.getBookingPayloadJson(), BookingRequestPayload.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize booking payload from message {}: {}", messageId, e.getMessage());
            throw new RuntimeException("Failed to read booking proposal details", e);
        }

        // Call BookingService to create the persistent Booking record
        Booking createdBooking = bookingService.createBookingFromProposal(request, payload);

        // Update request status
        request.setStatus(ServiceRequest.RequestStatus.BOOKING_CONFIRMED);
        ServiceRequest updatedRequest = serviceRequestRepository.save(request);
        log.info("Booking confirmed for request ID {}. Status updated to BOOKING_CONFIRMED. Created Booking ID: {}", requestId, createdBooking.getId());

        notificationService.createNotification(
                request.getProvider(),
                Notification.NotificationType.BOOKING_CONFIRMED,
                new NotificationPayload(request.getId(), createdBooking.getId(), "Booking confirmed by " + seeker.getFirstName(), seeker.getId())
        );

        return serviceRequestMapper.toDto(updatedRequest);
    }

    @Transactional
    public ServiceRequestResponseDTO rejectBooking(Long requestId, User seeker) {
        log.info("Attempting to reject booking for request ID {} by seeker ID {}", requestId, seeker.getId());
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Service Request not found with ID: " + requestId));

        if (!request.getSeeker().getId().equals(seeker.getId())) {
            log.warn("Unauthorized attempt to reject booking for request {}. User {} is not the seeker.", requestId, seeker.getId());
            throw new UnauthorizedAccessException("Only the service seeker can reject the booking proposal.");
        }

        if (request.getStatus() != ServiceRequest.RequestStatus.ACCEPTED) {
            log.warn("Attempt to reject booking for request {} failed. Status is not ACCEPTED, it is {}", requestId, request.getStatus());
            throw new IllegalStateException("Booking proposal can only be rejected if the request is currently accepted.");
        }

        // Status remains ACCEPTED upon rejection for now, allowing new proposals
        log.info("Booking proposal rejected for request ID {}. Status remains ACCEPTED.", requestId);

        notificationService.createNotification(
                request.getProvider(),
                Notification.NotificationType.REQUEST_DECLINED, // Reusing this type
                new NotificationPayload(request.getId(), null, "Booking proposal for request #"+request.getId()+" was declined by the client.", seeker.getId())
        );

        return serviceRequestMapper.toDto(request);
    }

    public ServiceRequestResponseDTO getRequestDetails(Long requestId, User user) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));

        validateRequestAccess(request, user);

        return serviceRequestMapper.toDto(request); // Using ServiceRequestMapper
    }

    public List<ServiceRequestResponseDTO> getProviderRequests(Long providerId) {
        return serviceRequestRepository.findByProviderIdOrderByCreatedAtDesc(providerId)
                .stream()
                .map(serviceRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ServiceRequestResponseDTO> getSeekerRequests(Long seekerId) {
        return serviceRequestRepository.findBySeekerIdOrderByCreatedAtDesc(seekerId)
                .stream()
                .map(serviceRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    private void validateRequestAccess(ServiceRequest request, User user) {
        if (!request.getSeeker().equals(user) && !request.getProvider().equals(user)) {
            throw new UnauthorizedAccessException("You don't have permission to view this request");
        }
    }
}