package com.servx.servx.controller;

import com.servx.servx.dto.BookingDTO;
import com.servx.servx.entity.User;
import com.servx.servx.enums.BookingStatus;
import com.servx.servx.enums.Role;
import com.servx.servx.exception.UnauthorizedAccessException;
import com.servx.servx.exception.UserNotFoundException;
import com.servx.servx.repository.UserRepository;
import com.servx.servx.service.BookingService;
import com.servx.servx.util.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    // Example: Get bookings based on role and status query parameter
    @GetMapping
    public ResponseEntity<Page<BookingDTO>> getMyBookings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = true) BookingStatus status,
            @PageableDefault(size = 10, sort = "scheduledStartTime") Pageable pageable) {

        log.info("Fetching bookings for user {} with status {}", userDetails.getUsername(), status);
        User user = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Page<BookingDTO> bookings;
        if (user.getRole() == Role.SERVICE_PROVIDER) {
            bookings = bookingService.getProviderBookings(user, status, pageable);
        } else if (user.getRole() == Role.SERVICE_SEEKER) {
            bookings = bookingService.getSeekerBookings(user, status, pageable);
        } else {
            log.warn("User {} has unexpected role {}", user.getEmail(), user.getRole());
            return ResponseEntity.badRequest().build(); // Or forbidden
        }
        return ResponseEntity.ok(bookings);
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Received request to cancel booking ID: {}", bookingId);
        User cancellingUser = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        try {
            bookingService.cancelBooking(bookingId, cancellingUser);
            return ResponseEntity.noContent().build(); // Indicate success with no content
        } catch (EntityNotFoundException | UnauthorizedAccessException | IllegalStateException e) {
            log.warn("Failed to cancel booking {}: {}", bookingId, e.getMessage());
            // Consider returning different statuses based on error (e.g., 403 Forbidden, 404 Not Found, 409 Conflict)
            // For simplicity, return 400 Bad Request for now
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error cancelling booking {}", bookingId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}