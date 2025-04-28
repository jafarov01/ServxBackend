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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

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
            return ResponseEntity.badRequest().build();
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
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException | UnauthorizedAccessException | IllegalStateException e) {
            log.warn("Failed to cancel booking {}: {}", bookingId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error cancelling booking {}", bookingId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/by-date")
    public ResponseEntity<List<BookingDTO>> getMyBookingsByDateRange(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Fetching bookings for user {} from {} to {}", userDetails.getUsername(), startDate, endDate);
        User user = findUser(userDetails);

        List<BookingDTO> bookings;
        if (user.getRole() == Role.SERVICE_PROVIDER) {
            bookings = bookingService.getProviderBookingsByDateRange(user, startDate, endDate);
        } else if (user.getRole() == Role.SERVICE_SEEKER) {
            bookings = bookingService.getSeekerBookingsByDateRange(user, startDate, endDate);
        } else {
            log.warn("User {} has unexpected role {}", user.getEmail(), user.getRole());
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(bookings);
    }

    @PostMapping("/{bookingId}/provider-complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markCompleteByProvider(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Received request from user {} to mark booking {} as complete (by provider).", userDetails.getUsername(), bookingId);
        User providerUser = findUser(userDetails);

        if (providerUser.getRole() != Role.SERVICE_PROVIDER) {
            log.warn("User {} attempted provider action but has role {}", userDetails.getUsername(), providerUser.getRole());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            bookingService.markBookingCompletedByProvider(bookingId, providerUser);
            return ResponseEntity.noContent().build();

        } catch (EntityNotFoundException e) {
            log.warn("Provider mark complete failed: Booking {} not found.", bookingId, e);
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedAccessException e) {
            log.warn("Provider mark complete failed: User {} not authorized for booking {}.", providerUser.getId(), bookingId, e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalStateException e) {
            log.warn("Provider mark complete failed: Invalid state for booking {}.", bookingId, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Unexpected error marking booking {} as complete by provider {}: {}", bookingId, providerUser.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{bookingId}/seeker-confirm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> confirmCompletionBySeeker(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Received request from user {} to confirm completion for booking {} (by seeker).", userDetails.getUsername(), bookingId);
        User seekerUser = findUser(userDetails);

        if (seekerUser.getRole() != Role.SERVICE_SEEKER) {
            log.warn("User {} attempted seeker action but has role {}", userDetails.getUsername(), seekerUser.getRole());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            bookingService.confirmCompletionBySeeker(bookingId, seekerUser);
            return ResponseEntity.noContent().build();

        } catch (EntityNotFoundException e) {
            log.warn("Seeker confirm completion failed: Booking {} not found.", bookingId, e);
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedAccessException e) {
            log.warn("Seeker confirm completion failed: User {} not authorized for booking {}.", seekerUser.getId(), bookingId, e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalStateException e) {
            log.warn("Seeker confirm completion failed: Invalid state for booking {}.", bookingId, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Unexpected error confirming completion for booking {} by seeker {}: {}", bookingId, seekerUser.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private User findUser(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new UnauthorizedAccessException("User details not found in security context.");
        }
        return userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userDetails.getUsername()));
    }
}