package com.servx.servx.service.Auth;

import com.servx.servx.dto.*;
import com.servx.servx.entity.*;
import com.servx.servx.enums.Role;
import com.servx.servx.exception.EmailAlreadyExistsException;
import com.servx.servx.exception.InvalidCredentialsException;
import com.servx.servx.exception.InvalidTokenException;
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
    private final ProfileRepository profileRepository;
    private final ProfileServiceAreaRepository profileServiceAreaRepository;
    private final AddressRepository addressRepository;
    private final ServiceAreaRepository serviceAreaRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationTokenService verificationTokenService;
    private final VerificationTokenRepository verificationTokenRepository;

    @Override
    public UserResponseDTO registerServiceSeeker(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already in use");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(encodedPassword)
                .phoneNumber(request.getPhoneNumber())
                .isVerified(false)
                .role(Role.SERVICE_SEEKER)
                .address(Address.builder()
                        .city(request.getAddress().getCity())
                        .country(request.getAddress().getCountry())
                        .zipCode(request.getAddress().getZipCode())
                        .addressLine(request.getAddress().getAddressLine())
                        .build())
                .build();

        userRepository.save(user);

        request.getLanguagesSpoken().forEach(language -> languageRepository.save(Language.builder()
                .user(user)
                .language(language)
                .build()));

        String verificationToken = verificationTokenService.createVerificationToken(user.getId()).getToken();
        emailService.sendVerificationEmail(user, verificationToken);

        return mapToUserResponseDTO(user);
    }

    @Override
    public UserResponseDTO registerServiceProvider(ServiceProviderRegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already in use");
        }

        // Encode the password
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // Build and save the user
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(encodedPassword)
                .phoneNumber(request.getPhoneNumber())
                .isVerified(false)
                .role(Role.SERVICE_PROVIDER)
                .education(request.getEducation())
                .address(Address.builder()
                        .city(request.getAddress().getCity())
                        .country(request.getAddress().getCountry())
                        .zipCode(request.getAddress().getZipCode())
                        .addressLine(request.getAddress().getAddressLine())
                        .build())
                .build();
        userRepository.save(user);

        // Save languages spoken
        request.getLanguagesSpoken().forEach(language -> languageRepository.save(Language.builder()
                .user(user)
                .language(language)
                .build()));

        // Save profiles and service areas
        request.getProfiles().forEach(profileDTO -> {
            Profile profile = Profile.builder()
                    .user(user)
                    .serviceCategory(ServiceCategory.builder()
                            .id(profileDTO.getServiceCategoryId())
                            .build())
                    .workExperience(profileDTO.getWorkExperience())
                    .build();
            profileRepository.save(profile);

            profileDTO.getServiceAreaIds().forEach(serviceAreaId -> profileServiceAreaRepository.save(ProfileServiceArea.builder()
                    .profile(profile)
                    .serviceArea(ServiceArea.builder()
                            .id(serviceAreaId)
                            .build())
                    .build()));
        });

        // Generate and send email verification token
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

        String token = JwtUtils.generateToken(user.getEmail(), user.getRole().toString());

        return AuthResponseDTO.builder()
                .token(token)
                .role(user.getRole().name())
                .build();
    }

    public ResponseEntity<String> verifyEmail(String token) {
        // Find the token in the database
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);

        // Validate token existence
        if (verificationToken == null) {
            throw new InvalidTokenException("Invalid or expired verification token.");
        }

        // Check if the token is expired
        if (verificationTokenService.isExpired(verificationToken)) {
            throw new InvalidTokenException("Verification token has expired. Please request a new one.");
        }

        // Retrieve the associated user by userId
        Long userId = verificationToken.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("User associated with this token does not exist."));

        // Check if the user is already verified
        if (user.isVerified()) {
            return ResponseEntity.badRequest().body("User email is already verified.");
        }

        // Mark the user as verified
        user.setVerified(true);
        userRepository.save(user);

        // Delete the used token to prevent reuse
        verificationTokenRepository.deleteById(verificationToken.getId());

        return ResponseEntity.ok("Email verified successfully. You can now log in.");
    }

    private UserResponseDTO mapToUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .languagesSpoken(languageRepository.findByUserId(user.getId())
                        .stream()
                        .map(Language::getLanguage)
                        .collect(Collectors.toList()))
                .address(UserResponseDTO.AddressDTO.builder()
                        .city(user.getAddress().getCity())
                        .country(user.getAddress().getCountry())
                        .zipCode(user.getAddress().getZipCode())
                        .addressLine(user.getAddress().getAddressLine())
                        .build())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .build();
    }

}
