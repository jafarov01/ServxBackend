package com.servx.servx.service;

import com.servx.servx.dto.*;
import com.servx.servx.entity.*;
import com.servx.servx.exception.UnauthorizedAccessException;
import com.servx.servx.repository.ServiceProfileRepository;
import com.servx.servx.repository.ServiceRequestRepository;
import com.servx.servx.repository.UserRepository;
import com.servx.servx.util.ServiceRequestMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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