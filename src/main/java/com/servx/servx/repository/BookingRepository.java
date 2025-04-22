package com.servx.servx.repository;

import com.servx.servx.entity.Booking;
import com.servx.servx.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List; // Import List if needed for non-paged methods

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find bookings for a specific seeker based on status, ordered by schedule time
    // Returns a Page for potential pagination in the "My Bookings" tab
    Page<Booking> findBySeekerIdAndStatusOrderByScheduledStartTimeAsc(Long seekerId, BookingStatus status, Pageable pageable);

    // Find bookings for a specific provider based on status, ordered by schedule time
    Page<Booking> findByProviderIdAndStatusOrderByScheduledStartTimeAsc(Long providerId, BookingStatus status, Pageable pageable);

    List<Booking> findByProviderIdAndScheduledStartTimeBetweenOrderByScheduledStartTimeAsc(
            Long providerId, Instant startTime, Instant endTime);

    List<Booking> findBySeekerIdAndScheduledStartTimeBetweenOrderByScheduledStartTimeAsc(
            Long seekerId, Instant startTime, Instant endTime);
}