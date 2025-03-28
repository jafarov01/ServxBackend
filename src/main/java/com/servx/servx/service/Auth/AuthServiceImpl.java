package com.servx.servx.service.Auth;

import com.servx.servx.dto.*;
import com.servx.servx.entity.*;
import com.servx.servx.enums.Role;
import com.servx.servx.exception.*;
import com.servx.servx.repository.*;
import com.servx.servx.service.Auth.interfaces.IAuthService;
import com.servx.servx.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {
    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationTokenService verificationTokenService;
    private final VerificationTokenRepository verificationTokenRepository;

    @Override
    public UserResponseDTO register(RegisterRequestDTO request) {
        // Check for existing email/phone
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already in use");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new PhoneNumberExistsException("Phone number already in use");
        }

        // Build User with Address
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .isVerified(false)
                .role(validateAndGetRole(request.getRole())) // Handle role conversion
                .address(Address.builder()
                        .city(request.getAddress().getCity())
                        .country(request.getAddress().getCountry())
                        .zipCode(request.getAddress().getZipCode())
                        .addressLine(request.getAddress().getAddressLine())
                        .build())
                .build();

        // Add languages via cascade
        user.setLanguagesSpoken(
                request.getLanguagesSpoken().stream()
                        .map(lang -> Language.builder().user(user).language(lang).build())
                        .collect(Collectors.toSet())
        );

        userRepository.save(user); // Cascades to address and languages

        // Send verification email
        String verificationToken = verificationTokenService.createVerificationToken(user.getId()).getToken();
        emailService.sendVerificationEmail(user, verificationToken);

        return mapToUserResponseDTO(user);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Email not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Wrong password");
        }

        return AuthResponseDTO.builder()
                .token(JwtUtils.generateToken(user.getEmail(), user.getRole().toString()))
                .role(user.getRole().name())
                .build();
    }

    @Override
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
                .languagesSpoken(user.getLanguagesSpoken().stream() // Use cascaded collection
                        .map(Language::getLanguage)
                        .collect(Collectors.toList()))
                .address(UserResponseDTO.AddressDTO.builder()
                        .city(user.getAddress().getCity())
                        .country(user.getAddress().getCountry())
                        .zipCode(user.getAddress().getZipCode())
                        .addressLine(user.getAddress().getAddressLine())
                        .build())
                .role(user.getRole().name())
                .build();
    }
}