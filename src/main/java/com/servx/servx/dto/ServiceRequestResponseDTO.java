package com.servx.servx.dto;

import com.servx.servx.entity.ServiceRequest;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestResponseDTO {
    private Long id;
    private String description;
    private ServiceRequest.SeverityLevel severity;
    private ServiceRequest.RequestStatus status;
    private AddressResponseDTO address;
    private LocalDateTime createdAt;
    private ServiceProfileDTO service;
    private UserResponseDTO provider;
}