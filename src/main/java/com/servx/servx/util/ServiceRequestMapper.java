package com.servx.servx.util;

import com.servx.servx.dto.*;
import com.servx.servx.entity.*;
import com.servx.servx.repository.ServiceProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class ServiceRequestMapper {

    private final ServiceProfileRepository serviceProfileRepository;

    public ServiceRequestMapper(ServiceProfileRepository serviceProfileRepository) {
        this.serviceProfileRepository = serviceProfileRepository;
    }

    // Maps ServiceRequest entity to ServiceRequestResponseDTO
    public ServiceRequestResponseDTO toDto(ServiceRequest request) {
        ServiceProfile service = serviceProfileRepository.findById(request.getService().getId())
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        return ServiceRequestResponseDTO.builder()
                .id(request.getId())
                .description(request.getDescription())
                .severity(request.getSeverity())
                .status(request.getStatus())
                .address(mapAddressToDTO(request.getAddress()))
                .createdAt(request.getCreatedAt())
                .service(mapServiceProfileToDTO(service))
                .provider(mapUserToDTO(request.getProvider()))
                .build();
    }

    // Maps Address entity to AddressResponseDTO
    private AddressResponseDTO mapAddressToDTO(RequestAddress address) {
        return new AddressResponseDTO(
                address.getAddressLine(),
                address.getCity(),
                address.getZipCode(),
                address.getCountry()
        );
    }

    // Maps ServiceProfile entity to ServiceProfileDTO
    private ServiceProfileDTO mapServiceProfileToDTO(ServiceProfile service) {
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
                rating, // Properly calculated rating
                service.getReviewCount()
        );
    }

    // Maps User entity to UserResponseDTO
    private UserResponseDTO mapUserToDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .build();
    }
}