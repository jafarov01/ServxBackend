package com.servx.servx.service;

import com.servx.servx.dto.*;
import com.servx.servx.entity.*;
import com.servx.servx.exception.UnauthorizedAccessException;
import com.servx.servx.repository.ServiceProfileRepository;
import com.servx.servx.repository.ServiceRequestRepository;
import com.servx.servx.repository.UserRepository;
import com.servx.servx.util.ServiceRequestMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceRequestService {
    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceProfileRepository serviceProfileRepository;
    private final UserRepository userRepository;
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

    // --- NEW METHOD: Confirm Booking ---
    @Transactional
    public ServiceRequestResponseDTO confirmBooking(Long requestId, User seeker) {
        log.info("Attempting to confirm booking for request ID {} by seeker ID {}", requestId, seeker.getId());
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Service Request not found with ID: " + requestId));

        // Validation: Only the seeker for this request can confirm
        if (!request.getSeeker().getId().equals(seeker.getId())) {
            log.warn("Unauthorized attempt to confirm booking for request {}. User {} is not the seeker.", requestId, seeker.getId());
            throw new UnauthorizedAccessException("Only the service seeker can confirm the booking.");
        }

        // Validation: Can only confirm if currently ACCEPTED (or potentially another state if needed)
        if (request.getStatus() != ServiceRequest.RequestStatus.ACCEPTED) {
            log.warn("Attempt to confirm booking for request {} failed. Status is not ACCEPTED, it is {}", requestId, request.getStatus());
            throw new IllegalStateException("Booking can only be confirmed if the request is currently accepted.");
        }

        // --- Main Logic ---
        // TODO: In the future, create a persistent Booking entity here if needed
        // Booking booking = bookingRepository.save(new Booking(...));
        // request.setBooking(booking); // Link request to booking

        // Update request status
        request.setStatus(ServiceRequest.RequestStatus.BOOKING_CONFIRMED);
        ServiceRequest updatedRequest = serviceRequestRepository.save(request);
        log.info("Booking confirmed for request ID {}. Status updated to BOOKING_CONFIRMED.", requestId);

        // --- Send Notification to Provider ---
        notificationService.createNotification(
                request.getProvider(), // Notify the provider
                Notification.NotificationType.BOOKING_CONFIRMED,
                new NotificationPayload(
                        request.getId(),
                        null, // bookingId if you created a Booking entity
                        "Your booking proposal for request #"+request.getId()+" was confirmed by the client.",
                        seeker.getId() // ID of the user who performed the action
                )
        );

        return serviceRequestMapper.toDto(updatedRequest);
    }

    // --- NEW METHOD: Reject Booking ---
    @Transactional
    public ServiceRequestResponseDTO rejectBooking(Long requestId, User seeker) {
        log.info("Attempting to reject booking for request ID {} by seeker ID {}", requestId, seeker.getId());
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Service Request not found with ID: " + requestId));

        // Validation: Only the seeker for this request can reject
        if (!request.getSeeker().getId().equals(seeker.getId())) {
            log.warn("Unauthorized attempt to reject booking for request {}. User {} is not the seeker.", requestId, seeker.getId());
            throw new UnauthorizedAccessException("Only the service seeker can reject the booking proposal.");
        }

        // Validation: Can only reject if currently ACCEPTED (or maybe if PROPOSED state existed)
        if (request.getStatus() != ServiceRequest.RequestStatus.ACCEPTED) {
            log.warn("Attempt to reject booking for request {} failed. Status is not ACCEPTED, it is {}", requestId, request.getStatus());
            throw new IllegalStateException("Booking proposal can only be rejected if the request is currently accepted.");
        }

        // --- Main Logic ---
        // Decide what status to revert to. Let's go back to ACCEPTED
        // meaning the request is still active but needs a new proposal/discussion.
        // OR introduce a BOOKING_REJECTED status if needed.
        // For now, let's just log it and keep status ACCEPTED. If status should change, uncomment below.
        // request.setStatus(RequestStatus.ACCEPTED); // Or potentially a specific REJECTED status
        // ServiceRequest updatedRequest = serviceRequestRepository.save(request);
        log.info("Booking proposal rejected for request ID {}. Status remains ACCEPTED (or implement specific rejected state).", requestId);

        // --- Send Notification to Provider ---
        // Use REQUEST_DECLINED type, or create a specific BOOKING_REJECTED type
        notificationService.createNotification(
                request.getProvider(), // Notify the provider
                Notification.NotificationType.REQUEST_DECLINED, // Reusing this, or create BOOKING_REJECTED
                new NotificationPayload(
                        request.getId(),
                        null,
                        "Your booking proposal for request #"+request.getId()+" was declined by the client.",
                        seeker.getId()
                )
        );

        // Return current state (which might not have changed if keeping ACCEPTED)
        return serviceRequestMapper.toDto(request); // Or updatedRequest if status changed
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