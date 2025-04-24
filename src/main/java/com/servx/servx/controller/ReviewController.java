package com.servx.servx.controller;

import com.servx.servx.dto.ReviewDTO;
import com.servx.servx.dto.ReviewRequestDTO;
import com.servx.servx.entity.User;
import com.servx.servx.exception.DuplicateEntryException;
import com.servx.servx.exception.UnauthorizedAccessException;
import com.servx.servx.exception.UserNotFoundException;
import com.servx.servx.repository.UserRepository;
import com.servx.servx.service.ReviewService;
import com.servx.servx.util.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews") // Base path for reviews
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository; // To lookup user from principal

    @PostMapping
    @PreAuthorize("isAuthenticated()") // Only logged-in users can submit reviews
    public ResponseEntity<?> submitReview( // Return ResponseEntity<?> for flexible error handling
                                           @AuthenticationPrincipal CustomUserDetails userDetails,
                                           @Valid @RequestBody ReviewRequestDTO reviewRequest) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User details not found.");
        }

        log.info("Received review submission from user {} for booking {}", userDetails.getUsername(), reviewRequest.getBookingId());

        try {
            User seeker = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));

            // Optional: Check if user is a seeker? Though service layer verifies ownership.
            // if(seeker.getRole() != Role.SERVICE_SEEKER) { return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); }

            reviewService.submitReview(seeker, reviewRequest);
            // Return 201 Created on successful submission
            return ResponseEntity.status(HttpStatus.CREATED).build();

        } catch (EntityNotFoundException e) {
            log.warn("Review submission failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        } catch (UnauthorizedAccessException e) {
            log.warn("Review submission failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403
        } catch (IllegalStateException e) {
            log.warn("Review submission failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400 (e.g., booking not completed)
        } catch (DuplicateEntryException e) {
            log.warn("Review submission failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 409 (already reviewed)
        } catch (Exception e) {
            log.error("Unexpected error submitting review from user {}: {}", userDetails.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred."); // 500
        }
    }

    @GetMapping("/service/{serviceId}")
    // No @PreAuthorize needed? Reviews are likely public. Add if needed.
    public ResponseEntity<Page<ReviewDTO>> getReviewsForService(
            @PathVariable Long serviceId,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Received request to fetch reviews for serviceId: {}", serviceId);
        try {
            Page<ReviewDTO> reviewDtoPage = reviewService.getReviewsForService(serviceId, pageable);
            return ResponseEntity.ok(reviewDtoPage);
        } catch (Exception e) {
            // Generic error handling, consider more specific catches if needed
            log.error("Error fetching reviews for serviceId {}: {}", serviceId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
