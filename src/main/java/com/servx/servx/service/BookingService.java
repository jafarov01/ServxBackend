package com.servx.servx.service;

import com.servx.servx.dto.BookingDTO;
import com.servx.servx.dto.BookingRequestPayload;
import com.servx.servx.entity.*;
import com.servx.servx.enums.BookingStatus;
import com.servx.servx.exception.UnauthorizedAccessException;
import com.servx.servx.repository.BookingRepository;
import com.servx.servx.util.BookingMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // Default transactionality for the service
@Slf4j
public class BookingService {

    private final NotificationService notificationService;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    // Method called by ServiceRequestService when a booking is confirmed
    public Booking createBookingFromProposal(ServiceRequest request, BookingRequestPayload payload) {
        log.info("Creating Booking from payload for ServiceRequest ID: {}", request.getId());

        if (payload.getAgreedDateTime() == null || payload.getDurationMinutes() == null) {
            throw new IllegalArgumentException("Booking requires agreed date/time and duration.");
        }

        String bookingNumber = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Booking newBooking = Booking.builder()
                .bookingNumber(bookingNumber)
                .scheduledStartTime(payload.getAgreedDateTime())
                .durationMinutes(payload.getDurationMinutes())
                .priceMin(payload.getPriceMin() != null ? payload.getPriceMin() : request.getService().getPrice()) // Fallback to service price?
                .priceMax(payload.getPriceMax() != null ? payload.getPriceMax() : payload.getPriceMin()) // Default max to min
                .notes(payload.getNotes())
                .locationAddressLine(request.getAddress().getAddressLine())
                .locationCity(request.getAddress().getCity())
                .locationZipCode(request.getAddress().getZipCode())
                .locationCountry(request.getAddress().getCountry())
                .serviceRequest(request)
                .provider(request.getProvider())
                .seeker(request.getSeeker())
                .service(request.getService())
                .status(BookingStatus.UPCOMING)
                .build();

        Booking savedBooking = bookingRepository.save(newBooking);
        log.info("Created Booking ID: {}, Number: {}", savedBooking.getId(), savedBooking.getBookingNumber());
        return savedBooking;
    }

    @Transactional(readOnly = true)
    public Page<BookingDTO> getProviderBookings(User provider, BookingStatus status, Pageable pageable) {
        log.info("Fetching {} bookings for provider ID {}", status, provider.getId());
        Page<Booking> bookingPage = bookingRepository.findByProviderIdAndStatusOrderByScheduledStartTimeAsc(provider.getId(), status, pageable);
        log.info("Found {} bookings on page {} for provider ID {}", bookingPage.getNumberOfElements(), pageable.getPageNumber(), provider.getId());
        // Map Page<Entity> to Page<DTO>
        List<BookingDTO> dtos = bookingPage.getContent().stream()
                .map(booking -> bookingMapper.toDto(booking)) // Use mapper
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, bookingPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<BookingDTO> getSeekerBookings(User seeker, BookingStatus status, Pageable pageable) {
        log.info("Fetching {} bookings for seeker ID {}", status, seeker.getId());
        Page<Booking> bookingPage = bookingRepository.findBySeekerIdAndStatusOrderByScheduledStartTimeAsc(seeker.getId(), status, pageable);
        log.info("Found {} bookings on page {} for seeker ID {}", bookingPage.getNumberOfElements(), pageable.getPageNumber(), seeker.getId());
        List<BookingDTO> dtos = bookingPage.getContent().stream()
                .map(bookingMapper::toDto) // Use mapper
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, bookingPage.getTotalElements());
    }

    @Transactional
    public void cancelBooking(Long bookingId, User cancellingUser) {
        log.info("User {} attempting to cancel booking {}", cancellingUser.getId(), bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with ID: " + bookingId));

        // Determine who the other party is for notification
        User otherParty;
        BookingStatus cancelledStatus;

        // Check if canceller is the seeker or provider
        if (booking.getSeeker().getId().equals(cancellingUser.getId())) {
            cancelledStatus = BookingStatus.CANCELLED_BY_SEEKER;
            otherParty = booking.getProvider();
            log.info("Booking {} cancelled by Seeker {}", bookingId, cancellingUser.getId());
        } else if (booking.getProvider().getId().equals(cancellingUser.getId())) {
            cancelledStatus = BookingStatus.CANCELLED_BY_PROVIDER;
            otherParty = booking.getSeeker();
            log.info("Booking {} cancelled by Provider {}", bookingId, cancellingUser.getId());
        } else {
            throw new UnauthorizedAccessException("User " + cancellingUser.getId() + " cannot cancel booking " + bookingId);
        }

        // Check if booking can be cancelled (e.g., only UPCOMING bookings)
        if (booking.getStatus() != BookingStatus.UPCOMING) {
            throw new IllegalStateException("Booking cannot be cancelled as it is not in UPCOMING status (current: " + booking.getStatus() + ")");
        }

        booking.setStatus(cancelledStatus);
        bookingRepository.save(booking);

        // Send notification to the other party
        notificationService.createNotification(
                otherParty,
                Notification.NotificationType.BOOKING_CANCELLED, // Add this type to NotificationType enum
                new NotificationPayload(
                        booking.getServiceRequest().getId(),
                        booking.getId(),
                        "Booking #" + booking.getBookingNumber() + " has been cancelled.",
                        cancellingUser.getId()
                )
        );
        log.info("Cancellation notification sent to user {}", otherParty.getId());
    }
}