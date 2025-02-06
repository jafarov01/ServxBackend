package com.servx.servx.service.Auth;

import com.servx.servx.entity.VerificationToken;
import com.servx.servx.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class VerificationTokenService {
    private final VerificationTokenRepository tokenRepository;

    @Autowired
    public VerificationTokenService(VerificationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public VerificationToken createVerificationToken(Long userId) {
        VerificationToken token = new VerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUserId(userId); // Store user ID directly
        token.setExpiryDate(LocalDateTime.now().plusHours(2)); // Token expires in 2 hours
        return tokenRepository.save(token);
    }

    public VerificationToken resendVerificationToken(Long userId) {
        tokenRepository.deleteByUserId(userId);
        return createVerificationToken(userId);
    }

    public boolean isExpired(VerificationToken token) {
        return LocalDateTime.now().isAfter(token.getExpiryDate());
    }
}