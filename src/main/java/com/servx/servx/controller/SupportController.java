package com.servx.servx.controller;

import com.servx.servx.dto.SupportRequestDTO;
import com.servx.servx.exception.UserNotFoundException;
import com.servx.servx.service.SupportService;
import com.servx.servx.util.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/support") // Base path for support-related endpoints
@RequiredArgsConstructor
@Slf4j
public class SupportController {

    private final SupportService supportService;

    @PostMapping("/request")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> submitSupportRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SupportRequestDTO request
    ) {
        if (userDetails == null) {
            log.warn("Unauthorized attempt to send support request.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Received support request from user: {}", userDetails.getUsername());

        try {
            supportService.sendSupportRequest(userDetails, request);
            log.info("Support request processed successfully for user: {}", userDetails.getUsername());
            return ResponseEntity.noContent().build();

        } catch (UserNotFoundException e) {
            log.error("Error processing support request: User not found - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (MailException e) {
            log.error("Error sending support email for user {}: {}", userDetails.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Unexpected error processing support request for user {}: {}", userDetails.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
