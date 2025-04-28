package com.servx.servx.controller;

import com.servx.servx.dto.ServiceRequestDTO;
import com.servx.servx.dto.ServiceRequestResponseDTO;
import com.servx.servx.entity.User;
import com.servx.servx.exception.UserNotFoundException;
import com.servx.servx.repository.UserRepository;
import com.servx.servx.service.ServiceRequestService;
import com.servx.servx.util.CustomUserDetails;
import com.servx.servx.util.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceRequestResponseDTO createServiceRequest(
            @RequestBody @Valid ServiceRequestDTO requestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User seeker = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return serviceRequestService.createServiceRequest(requestDTO, seeker);
    }

    @PatchMapping("/{id}/accept")
    public ServiceRequestResponseDTO acceptServiceRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User provider = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return serviceRequestService.acceptRequest(id, provider);
    }

    @GetMapping("/{id}")
    public ServiceRequestResponseDTO getServiceRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return serviceRequestService.getRequestDetails(id, user);
    }

    @GetMapping
    public List<ServiceRequestResponseDTO> getUserRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "seeker") String type
    ) {
        User user = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return type.equalsIgnoreCase("provider")
                ? serviceRequestService.getProviderRequests(user.getId())
                : serviceRequestService.getSeekerRequests(user.getId());
    }

    @PostMapping("/{requestId}/confirm-booking/{messageId}")
    public ResponseEntity<ServiceRequestResponseDTO> confirmBooking(
            @PathVariable("requestId") Long requestId,
            @PathVariable("messageId") Long messageId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Received request to confirm booking for request ID {} from message ID {}", requestId, messageId);
        User seeker = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        ServiceRequestResponseDTO updatedRequest = serviceRequestService.confirmBooking(requestId, messageId, seeker);
        return ResponseEntity.ok(updatedRequest);
    }

    @PostMapping("/{id}/reject-booking")
    public ResponseEntity<ServiceRequestResponseDTO> rejectBooking(
            @PathVariable("id") Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Received request to reject booking for request ID: {}", requestId);
        User seeker = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        ServiceRequestResponseDTO updatedRequest = serviceRequestService.rejectBooking(requestId, seeker);
        return ResponseEntity.ok(updatedRequest);
    }
}
