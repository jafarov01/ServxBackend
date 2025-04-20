package com.servx.servx.util;

import com.servx.servx.dto.*;
import com.servx.servx.entity.*;
import com.servx.servx.repository.ServiceProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

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
                .seeker(mapUserToDTO(request.getSeeker()))
                .build();
    }

    // Maps Address entity to AddressResponseDTO
    public AddressResponseDTO mapAddressToDTO(RequestAddress address) {
        return new AddressResponseDTO(
                address.getAddressLine(),
                address.getCity(),
                address.getZipCode(),
                address.getCountry()
        );
    }

    // Maps AddressRequestDTO to RequestAddress
    public RequestAddress mapAddress(AddressRequestDTO addressDTO) {
        return RequestAddress.builder()
                .addressLine(addressDTO.getAddressLine())
                .city(addressDTO.getCity())
                .zipCode(addressDTO.getZipCode())
                .country(addressDTO.getCountry())
                .build();
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
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .languagesSpoken(user.getLanguagesSpoken().stream()
                        .map(Language::getLanguage)
                        .collect(Collectors.toList()))
                .address(AddressResponseDTO.builder()
                        .city(user.getAddress().getCity())
                        .country(user.getAddress().getCountry())
                        .zipCode(user.getAddress().getZipCode())
                        .addressLine(user.getAddress().getAddressLine())
                        .build())
                .role(user.getRole().name())
                .education(user.getEducation())
                .build();
    }
}