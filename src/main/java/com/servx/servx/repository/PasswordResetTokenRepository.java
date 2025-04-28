package com.servx.servx.repository;

import com.servx.servx.entity.PasswordResetToken;
import com.servx.servx.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    void deleteByExpiryDateBefore(Instant now);

    List<PasswordResetToken> findByUser(User user);
}
