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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookingService {

    private final NotificationService notificationService;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

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
                .priceMin(payload.getPriceMin() != null ? payload.getPriceMin() : request.getService().getPrice())
                .priceMax(payload.getPriceMax() != null ? payload.getPriceMax() : payload.getPriceMin())
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
        List<BookingDTO> dtos = bookingPage.getContent().stream()
                .map(booking -> bookingMapper.toDto(booking))
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, bookingPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<BookingDTO> getSeekerBookings(User seeker, BookingStatus status, Pageable pageable) {
        log.info("Fetching {} bookings for seeker ID {}", status, seeker.getId());
        Page<Booking> bookingPage = bookingRepository.findBySeekerIdAndStatusOrderByScheduledStartTimeAsc(seeker.getId(), status, pageable);
        log.info("Found {} bookings on page {} for seeker ID {}", bookingPage.getNumberOfElements(), pageable.getPageNumber(), seeker.getId());
        List<BookingDTO> dtos = bookingPage.getContent().stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, bookingPage.getTotalElements());
    }

    public void markBookingCompletedByProvider(Long bookingId, User provider) {
        log.info("Provider {} attempting to mark booking {} as completed.", provider.getId(), bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with ID: " + bookingId));

        if (!booking.getProvider().getId().equals(provider.getId())) {
            log.warn("Unauthorized attempt by user {} to mark booking {} as completed (provider is {}).",
                    provider.getId(), bookingId, booking.getProvider().getId());
            throw new UnauthorizedAccessException("User is not the provider for this booking.");
        }

        if (booking.getStatus() != BookingStatus.UPCOMING) {
            log.warn("Cannot mark booking {} as completed by provider. Current status: {}", bookingId, booking.getStatus());
            throw new IllegalStateException("Booking can only be marked as complete if status is UPCOMING.");
        }

        booking.setProviderMarkedComplete(true);
        bookingRepository.save(booking);

        User seeker = booking.getSeeker();
        log.info("Sending PROVIDER_MARKED_COMPLETE notification to seeker {} for booking {}", seeker.getId(), bookingId);
        notificationService.createNotification(
                seeker,
                Notification.NotificationType.PROVIDER_MARKED_COMPLETE,
                new NotificationPayload(
                        booking.getServiceRequest().getId(),
                        booking.getId(),
                        String.format("Provider %s marked booking #%s as completed. Please confirm.",
                                provider.getFirstName(), booking.getBookingNumber()),
                        provider.getId()
                )
        );

        log.info("Provider {} successfully marked booking {} as completed (pending seeker confirmation).", provider.getId(), bookingId);
    }

    public void confirmCompletionBySeeker(Long bookingId, User seeker) {
        log.info("Seeker {} attempting to confirm completion for booking {}.", seeker.getId(), bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with ID: " + bookingId));

        if (!booking.getSeeker().getId().equals(seeker.getId())) {
            log.warn("Unauthorized attempt by user {} to confirm completion for booking {} (seeker is {}).",
                    seeker.getId(), bookingId, booking.getSeeker().getId());
            throw new UnauthorizedAccessException("User is not the seeker for this booking.");
        }

        if (!booking.isProviderMarkedComplete()) {
            log.warn("Seeker {} attempted to confirm booking {} before provider marked it complete.", seeker.getId(), bookingId);
            throw new IllegalStateException("Provider has not marked this booking as complete yet.");
        }

        if (booking.getStatus() != BookingStatus.UPCOMING) {
            log.warn("Cannot confirm completion for booking {}. Current status: {}", bookingId, booking.getStatus());
            if (booking.getStatus() == BookingStatus.COMPLETED) {
                log.info("Booking {} already completed, confirmation action redundant.", bookingId);
                return;
            }
            throw new IllegalStateException("Booking can only be confirmed if status is UPCOMING.");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);
        log.info("Booking {} status updated to COMPLETED by seeker {}.", bookingId, seeker.getId());

        User provider = booking.getProvider();
        log.info("Sending SEEKER_CONFIRMED_COMPLETION notification to provider {} for booking {}", provider.getId(), bookingId);
        notificationService.createNotification(
                provider,
                Notification.NotificationType.SEEKER_CONFIRMED_COMPLETION,
                new NotificationPayload(
                        booking.getServiceRequest().getId(),
                        booking.getId(),
                        String.format("Seeker %s confirmed completion for booking #%s.",
                                seeker.getFirstName(), booking.getBookingNumber()),
                        seeker.getId()
                )
        );
        log.info("Completion confirmation notification sent to provider {}", provider.getId());
    }

    @Transactional
    public void cancelBooking(Long bookingId, User cancellingUser) {
        log.info("User {} attempting to cancel booking {}", cancellingUser.getId(), bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with ID: " + bookingId));

        User otherParty;
        BookingStatus cancelledStatus;

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

        if (booking.getStatus() != BookingStatus.UPCOMING) {
            throw new IllegalStateException("Booking cannot be cancelled as it is not in UPCOMING status (current: " + booking.getStatus() + ")");
        }

        booking.setStatus(cancelledStatus);
        bookingRepository.save(booking);

        notificationService.createNotification(
                otherParty,
                Notification.NotificationType.BOOKING_CANCELLED,
                new NotificationPayload(
                        booking.getServiceRequest().getId(),
                        booking.getId(),
                        "Booking #" + booking.getBookingNumber() + " has been cancelled.",
                        cancellingUser.getId()
                )
        );
        log.info("Cancellation notification sent to user {}", otherParty.getId());
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getProviderBookingsByDateRange(User provider, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching bookings for provider ID {} from {} to {}", provider.getId(), startDate, endDate);
        Instant startInstant = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endInstant = endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        List<Booking> bookings = bookingRepository.findByProviderIdAndScheduledStartTimeBetweenOrderByScheduledStartTimeAsc(
                provider.getId(), startInstant, endInstant
        );
        log.info("Found {} bookings for provider ID {} in date range", bookings.size(), provider.getId());
        return mapBookingListToDto(bookings);
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getSeekerBookingsByDateRange(User seeker, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching bookings for seeker ID {} from {} to {}", seeker.getId(), startDate, endDate);
        Instant startInstant = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endInstant = endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        List<Booking> bookings = bookingRepository.findBySeekerIdAndScheduledStartTimeBetweenOrderByScheduledStartTimeAsc(
                seeker.getId(), startInstant, endInstant
        );
        log.info("Found {} bookings for seeker ID {} in date range", bookings.size(), seeker.getId());
        return mapBookingListToDto(bookings);
    }

    private List<BookingDTO> mapBookingListToDto(List<Booking> bookings) {
        return bookings.stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }
}