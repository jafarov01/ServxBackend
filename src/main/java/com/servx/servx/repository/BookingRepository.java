package com.servx.servx.repository;

import com.servx.servx.entity.Booking;
import com.servx.servx.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findBySeekerIdAndStatusOrderByScheduledStartTimeAsc(Long seekerId, BookingStatus status, Pageable pageable);

    Page<Booking> findByProviderIdAndStatusOrderByScheduledStartTimeAsc(Long providerId, BookingStatus status, Pageable pageable);

    List<Booking> findByProviderIdAndScheduledStartTimeBetweenOrderByScheduledStartTimeAsc(
            Long providerId, Instant startTime, Instant endTime);

    List<Booking> findBySeekerIdAndScheduledStartTimeBetweenOrderByScheduledStartTimeAsc(
            Long seekerId, Instant startTime, Instant endTime);
}