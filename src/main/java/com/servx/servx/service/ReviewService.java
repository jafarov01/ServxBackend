package com.servx.servx.service;

import com.servx.servx.dto.ReviewDTO;
import com.servx.servx.dto.ReviewRequestDTO;
import com.servx.servx.entity.Booking;
import com.servx.servx.entity.Review;
import com.servx.servx.entity.ServiceProfile;
import com.servx.servx.entity.User;
import com.servx.servx.enums.BookingStatus;
import com.servx.servx.exception.*;
import org.springframework.data.domain.Pageable;
import com.servx.servx.repository.BookingRepository;
import com.servx.servx.repository.ReviewRepository;
import com.servx.servx.repository.ServiceProfileRepository;
import com.servx.servx.util.ReviewMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final ServiceProfileRepository serviceProfileRepository;
    private final ReviewMapper reviewMapper;

    @Transactional // Ensure review saving and aggregate update are atomic
    public void submitReview(User seeker, ReviewRequestDTO reviewRequest) {
        log.info("Attempting review submission by user {} for booking {}", seeker.getId(), reviewRequest.getBookingId());

        // 1. Fetch the Booking
        Booking booking = bookingRepository.findById(reviewRequest.getBookingId())
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with ID: " + reviewRequest.getBookingId()));

        // 2. Verify User is the Seeker for this Booking
        if (!booking.getSeeker().getId().equals(seeker.getId())) {
            log.warn("Unauthorized review attempt: User {} is not the seeker for booking {}.", seeker.getId(), booking.getId());
            throw new UnauthorizedAccessException("User is not authorized to review this booking.");
        }

        // 3. Verify Booking Status is COMPLETED
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            log.warn("Review attempt on non-completed booking {}. Status: {}", booking.getId(), booking.getStatus());
            throw new IllegalStateException("Reviews can only be submitted for completed bookings.");
        }

        // 4. Check if Review already exists for this booking by this user
        if (reviewRepository.existsByBookingIdAndUserId(booking.getId(), seeker.getId())) {
            log.warn("Duplicate review attempt by user {} for booking {}.", seeker.getId(), booking.getId());
            throw new DuplicateEntryException("You have already submitted a review for this booking.");
        }

        // 5. Create and Save the Review entity
        Review newReview = Review.builder()
                .booking(booking) // Link to the booking
                .service(booking.getService()) // Link to the specific service profile
                .user(seeker) // Link to the reviewer (seeker)
                .rating(reviewRequest.getRating())
                .comment(reviewRequest.getComment())
                // createdAt will be set automatically by @CreationTimestamp
                .build();
        reviewRepository.save(newReview);
        log.info("Review saved successfully for booking {}.", booking.getId());

        // 6. Update ServiceProfile aggregates (rating sum and review count)
        ServiceProfile serviceProfile = booking.getService(); // Already fetched via Booking relationship
        // Ensure thread safety if needed, though @Transactional helps
        serviceProfile.setReviewCount(serviceProfile.getReviewCount() + 1);
        serviceProfile.setRating(serviceProfile.getRating() + reviewRequest.getRating()); // Add new rating to sum
        serviceProfileRepository.save(serviceProfile);
        log.info("Updated aggregates for ServiceProfile {}. New count: {}, New rating sum: {}",
                serviceProfile.getId(), serviceProfile.getReviewCount(), serviceProfile.getRating());

        // Optionally: Send notification to provider about new review?
    }

    @Transactional(readOnly = true) // Read-only transaction
    public Page<ReviewDTO> getReviewsForService(Long serviceId, Pageable pageable) {
        log.info("Fetching reviews for serviceId {} with pagination: {}", serviceId, pageable);

        if (!serviceProfileRepository.existsById(serviceId)) {
             throw new EntityNotFoundException("ServiceProfile not found with ID: " + serviceId);
        }

        Page<Review> reviewPage = reviewRepository.findByServiceIdOrderByCreatedAtDesc(serviceId, pageable);
        log.info("Found {} reviews on page {} for serviceId {}", reviewPage.getNumberOfElements(), pageable.getPageNumber(), serviceId);

        return reviewMapper.toDtoPage(reviewPage);
    }
}
