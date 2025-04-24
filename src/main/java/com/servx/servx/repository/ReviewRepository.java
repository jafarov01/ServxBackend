package com.servx.servx.repository;

import com.servx.servx.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByBookingIdAndUserId(Long bookingId, Long userId);

    Optional<Review> findByBookingIdAndUserId(Long bookingId, Long userId);

    Page<Review> findByServiceIdOrderByCreatedAtDesc(Long serviceId, Pageable pageable);

    List<Review> findByServiceIdOrderByCreatedAtDesc(Long serviceId);
}
