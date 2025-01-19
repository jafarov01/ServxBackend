package com.servx.servx.service.impl;

import com.servx.servx.dto.LoginRequestDTO;
import com.servx.servx.dto.RegisterRequestDTO;
import com.servx.servx.dto.ServiceProviderRegisterRequestDTO;
import com.servx.servx.dto.UserResponseDTO;
import com.servx.servx.entity.*;
import com.servx.servx.enums.Role;
import com.servx.servx.exception.EmailAlreadyExistsException;
import com.servx.servx.exception.InvalidCredentialsException;
import com.servx.servx.repository.*;
import com.servx.servx.service.interfaces.IAuthService;
import com.servx.servx.util.JwtUtils;
import lombok.RequiredArgsConstructor;
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

        return mapToUserResponseDTO(user);
    }

    @Override
    public String login(LoginRequestDTO request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Email not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Wrong password");
        }

        return JwtUtils.generateToken(user.getEmail(), user.getRole().toString());
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
