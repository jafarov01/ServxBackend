package com.servx.servx.service.Auth;

import com.servx.servx.dto.*;
import com.servx.servx.entity.*;
import com.servx.servx.enums.Role;
import com.servx.servx.exception.*;
import com.servx.servx.repository.*;
import com.servx.servx.util.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationTokenService verificationTokenService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtUtils jwtUtils;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    public UserResponseDTO register(RegisterRequestDTO request) {
        // check for existing email/phone
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already in use");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new PhoneNumberExistsException("Phone number already in use");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .isVerified(false)
                .role(Role.valueOf(request.getRole()))
                .education(null)
                .address(Address.builder()
                        .city(request.getAddress().getCity())
                        .country(request.getAddress().getCountry())
                        .zipCode(request.getAddress().getZipCode())
                        .addressLine(request.getAddress().getAddressLine())
                        .build())
                .build();

        user.setLanguagesSpoken(
                request.getLanguagesSpoken().stream()
                        .map(lang -> Language.builder().user(user).language(lang).build())  // Map strings to Language entities
                        .collect(Collectors.toList())
        );

        userRepository.save(user);

        String verificationToken = verificationTokenService.createVerificationToken(user.getId()).getToken();
        emailService.sendVerificationEmail(user, verificationToken);

        return mapToUserResponseDTO(user);
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Email not found"));

        if (!user.isVerified()) {
            throw new UnverifiedUserException("Email not verified. Please check your inbox.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Wrong password");
        }

        return AuthResponseDTO.builder()
                .token(jwtUtils.generateToken(user.getEmail(), user.getRole().toString(), user.getId())) // Pass userId here
                .role(user.getRole().name())
                .build();
    }

    public ResponseEntity<String> verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
        if (verificationToken == null) {
            throw new InvalidTokenException("Invalid or expired verification token.");
        }

        if (verificationTokenService.isExpired(verificationToken)) {
            throw new InvalidTokenException("Token expired. Request a new one.");
        }

        User user = userRepository.findById(verificationToken.getUserId())
                .orElseThrow(() -> new InvalidTokenException("User not found."));

        if (user.isVerified()) {
            return ResponseEntity.badRequest().body("Email already verified.");
        }

        user.setVerified(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken); // Invalidate token

        return ResponseEntity.ok("Email verified successfully.");
    }

    public void initiatePasswordReset(String email) {
        Optional<User> userOptional = userRepository.findByEmailIgnoreCase(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(token, user);

             List<PasswordResetToken> existingTokens = passwordResetTokenRepository.findByUser(user);
             if (!existingTokens.isEmpty()) {
                 passwordResetTokenRepository.deleteAll(existingTokens);
             }

            passwordResetTokenRepository.save(resetToken);

            String resetLink = frontendBaseUrl + "/reset-password.html?token=" + token;

            try {
                emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetLink);
            } catch (Exception ignored) {
            }

        }
    }

    public void completePasswordReset(String token, String newPassword) {

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    return new InvalidTokenException("Invalid password reset token provided.");
                });

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new InvalidTokenException("Password reset token has expired.");
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new InvalidPasswordException("Password must be at least 8 characters long.");
        }

        User user = resetToken.getUser();
        if (user == null) {
            passwordResetTokenRepository.delete(resetToken);
            throw new InvalidTokenException("Invalid token state: No associated user.");
        }


        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);

        emailService.sendPasswordResetConfirmationEmail(user.getEmail(), user.getFirstName());
    }


    private Role validateAndGetRole(String roleStr) {
        if (roleStr == null || roleStr.isEmpty()) {
            return Role.SERVICE_SEEKER; // Schema default
        }
        try {
            return Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleException("Invalid role: " + roleStr);
        }
    }

    private UserResponseDTO mapToUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .languagesSpoken(user.getLanguagesSpoken().stream()
                        .map(Language::getLanguage)
                        .collect(Collectors.toList()))
                .address(AddressResponseDTO.builder()
                        .city(user.getAddress().getCity())
                        .country(user.getAddress().getCountry())
                        .zipCode(user.getAddress().getZipCode())
                        .addressLine(user.getAddress().getAddressLine())
                        .build())
                .role(user.getRole().name())
                .education(user.getEducation())
                .build();
    }
}