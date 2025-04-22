package com.servx.servx.util;

import com.servx.servx.dto.*;
import com.servx.servx.entity.*;
import com.servx.servx.repository.ServiceProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ServiceRequestMapper {
    private final ServiceProfileRepository serviceProfileRepository;

    public ServiceRequestResponseDTO toDto(ServiceRequest request) {

        ServiceProfile serviceEntity = request.getService();
        if (serviceEntity == null) {
            throw new EntityNotFoundException("Service relationship not found on ServiceRequest with ID: " + request.getId());
        }

        return ServiceRequestResponseDTO.builder()
                .id(request.getId())
                .description(request.getDescription())
                .severity(request.getSeverity())
                .status(request.getStatus())
                .address(mapAddressToDTO(request.getAddress())) // Use existing helper
                .createdAt(request.getCreatedAt()) // Assuming correct type mapping

                // *** THE FIX ***
                // Use the constructor of ServiceProfileDTO directly
                .service(new ServiceProfileDTO(serviceEntity))

                .provider(mapUserToDTO(request.getProvider())) // Use existing helper
                .seeker(mapUserToDTO(request.getSeeker()))     // Use existing helper
                .build();
    }

    // Maps Address entity to AddressResponseDTO - Stays the same
    // Added null check
    public AddressResponseDTO mapAddressToDTO(RequestAddress address) {
        if (address == null) return null;
        return new AddressResponseDTO(
                address.getAddressLine(),
                address.getCity(),
                address.getZipCode(),
                address.getCountry()
        );
    }

    // Maps AddressRequestDTO to RequestAddress - Stays the same
    // Added null check
    public RequestAddress mapAddress(AddressRequestDTO addressDTO) {
        if (addressDTO == null) return null;
        return RequestAddress.builder()
                .addressLine(addressDTO.getAddressLine())
                .city(addressDTO.getCity())
                .zipCode(addressDTO.getZipCode())
                .country(addressDTO.getCountry())
                .build();
    }

    // Maps User entity to UserResponseDTO - Stays the same
    // Added null checks for safety
    private UserResponseDTO mapUserToDTO(User user) {
        if (user == null) return null;

        AddressResponseDTO addressDTO = null;
        if (user.getAddress() != null) {
            addressDTO = AddressResponseDTO.builder()
                    .city(user.getAddress().getCity())
                    .country(user.getAddress().getCountry())
                    .zipCode(user.getAddress().getZipCode())
                    .addressLine(user.getAddress().getAddressLine())
                    .build();
        }

        List<String> languages = user.getLanguagesSpoken() != null ?
                user.getLanguagesSpoken().stream()
                        .map(Language::getLanguage)
                        .collect(Collectors.toList()) :
                Collections.emptyList(); // Use empty list if null

        return UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .languagesSpoken(languages)
                .address(addressDTO)
                .role(user.getRole() != null ? user.getRole().name() : null) // Handle null role
                .education(user.getEducation())
                .build();
    }

}