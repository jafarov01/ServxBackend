package com.servx.servx.service;

import com.servx.servx.dto.*;
import com.servx.servx.entity.*;
import com.servx.servx.exception.UnauthorizedAccessException;
import com.servx.servx.repository.ServiceProfileRepository;
import com.servx.servx.repository.ServiceRequestRepository;
import com.servx.servx.repository.UserRepository;
import com.servx.servx.util.ServiceRequestMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
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
                .address(mapAddress(dto.getAddress()))
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

    public ServiceRequestResponseDTO getRequestDetails(Long requestId, User user) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));

        validateRequestAccess(request, user);

        return serviceRequestMapper.toDto(request); // Using ServiceRequestMapper
    }

    public List<ServiceRequestResponseDTO> getProviderRequests(Long providerId) {
        return serviceRequestRepository.findByProviderIdOrderByCreatedAtDesc(providerId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ServiceRequestResponseDTO> getSeekerRequests(Long seekerId) {
        return serviceRequestRepository.findBySeekerIdOrderByCreatedAtDesc(seekerId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private void validateRequestAccess(ServiceRequest request, User user) {
        if (!request.getSeeker().equals(user) && !request.getProvider().equals(user)) {
            throw new UnauthorizedAccessException("You don't have permission to view this request");
        }
    }

    private RequestAddress mapAddress(AddressRequestDTO addressDTO) {
        return RequestAddress.builder()
                .addressLine(addressDTO.getAddressLine())
                .city(addressDTO.getCity())
                .zipCode(addressDTO.getZipCode())
                .country(addressDTO.getCountry())
                .build();
    }

    private ServiceRequestResponseDTO mapToResponseDTO(ServiceRequest request) {
        return new ServiceRequestResponseDTO(
                request.getId(),
                request.getDescription(),
                request.getSeverity(),
                request.getStatus(),
                mapToAddressResponseDTO(request.getAddress()),
                request.getCreatedAt(),
                mapToServiceProfileDTO(request.getService()),
                mapToUserResponseDTO(request.getProvider())
        );
    }

    private AddressResponseDTO mapToAddressResponseDTO(RequestAddress address) {
        return new AddressResponseDTO(
                address.getAddressLine(),
                address.getCity(),
                address.getZipCode(),
                address.getCountry()
        );
    }

    private ServiceProfileDTO mapToServiceProfileDTO(ServiceProfile service) {
        double rating = service.getReviewCount() > 0
                ? service.getRating() / service.getReviewCount()
                : 0.0;

        return new ServiceProfileDTO(
                service.getId(),
                service.getUser().getFirstName() + " " + service.getUser().getLastName(),
                service.getCategory().getName(),
                service.getServiceArea().getName(),
                service.getWorkExperience(),
                service.getPrice(),
                rating, // Now properly calculated
                service.getReviewCount()
        );
    }

    private UserResponseDTO mapToUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .build();
    }
}