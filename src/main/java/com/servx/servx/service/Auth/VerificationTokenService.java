package com.servx.servx.service.Auth;

import com.servx.servx.entity.User;
import com.servx.servx.entity.VerificationToken;
import com.servx.servx.exception.AlreadyVerifiedException;
import com.servx.servx.exception.UserNotFoundException;
import com.servx.servx.repository.UserRepository;
import com.servx.servx.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {
    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    public VerificationToken createVerificationToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.isVerified()) {
            throw new AlreadyVerifiedException("User already verified");
        }

        VerificationToken token = new VerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUserId(userId);
        token.setExpiryDate(LocalDateTime.now().plusHours(2));
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