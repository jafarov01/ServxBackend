package com.servx.servx.repository;

import com.servx.servx.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    VerificationToken findByToken(String token);
    VerificationToken findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
