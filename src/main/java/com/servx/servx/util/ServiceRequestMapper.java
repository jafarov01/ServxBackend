package com.servx.servx.util;

import com.servx.servx.dto.*;
import com.servx.servx.entity.*;
import com.servx.servx.repository.ServiceProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ServiceRequestMapper {
    private final ServiceProfileRepository serviceProfileRepository;

    @Value("${app.base-url}")
    private String appBaseUrl;

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
                .address(mapAddressToDTO(request.getAddress()))
                .createdAt(request.getCreatedAt())

                .service(new ServiceProfileDTO(serviceEntity))

                .provider(mapUserToDTO(request.getProvider()))
                .seeker(mapUserToDTO(request.getSeeker()))
                .build();
    }

    public AddressResponseDTO mapAddressToDTO(RequestAddress address) {
        if (address == null) return null;
        return new AddressResponseDTO(
                address.getAddressLine(),
                address.getCity(),
                address.getZipCode(),
                address.getCountry()
        );
    }

    public RequestAddress mapAddress(AddressRequestDTO addressDTO) {
        if (addressDTO == null) return null;
        return RequestAddress.builder()
                .addressLine(addressDTO.getAddressLine())
                .city(addressDTO.getCity())
                .zipCode(addressDTO.getZipCode())
                .country(addressDTO.getCountry())
                .build();
    }

    private UserResponseDTO mapUserToDTO(User user) {
        String baseUrl = appBaseUrl;


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
                Collections.emptyList();

        return UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profilePhotoUrl(constructFullUrl(baseUrl, user.getProfilePhotoUrl()))
                .languagesSpoken(languages)
                .address(addressDTO)
                .role(user.getRole() != null ? user.getRole().name() : null)
                .education(user.getEducation())
                .build();
    }

    private String constructFullUrl(String baseUrl, String path) {
        if (path == null || path.isBlank() || baseUrl == null || baseUrl.isBlank()) {
            return null;
        }
        if (path.toLowerCase().startsWith("http://") || path.toLowerCase().startsWith("https://")) {
            return path;
        }
        String cleanBaseUrl = baseUrl.replaceAll("/$", "");
        String cleanPath = path.startsWith("/") ? path : "/" + path;
        return cleanBaseUrl + cleanPath;
    }
}