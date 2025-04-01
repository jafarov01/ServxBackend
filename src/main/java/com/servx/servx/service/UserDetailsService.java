package com.servx.servx.service;

import com.servx.servx.dto.UserDetailsResponseDTO;
import com.servx.servx.entity.Language;
import com.servx.servx.entity.User;
import com.servx.servx.exception.UserNotFoundException;
import com.servx.servx.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsResponseDTO getCurrentUserDetails() {
        String email = getAuthenticatedUserEmail();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        return mapToUserDetailsDTO(user);
    }

    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication.getName());
        return authentication.getName();
    }

    private UserDetailsResponseDTO mapToUserDetailsDTO(User user) {
        return UserDetailsResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .role(user.getRole().name())
                .address(mapAddressDTO(user))
                .languagesSpoken(mapLanguages(user))
                .build();
    }

    private UserDetailsResponseDTO.AddressDTO mapAddressDTO(User user) {
        return UserDetailsResponseDTO.AddressDTO.builder()
                .city(user.getAddress().getCity())
                .country(user.getAddress().getCountry())
                .zipCode(user.getAddress().getZipCode())
                .addressLine(user.getAddress().getAddressLine())
                .build();
    }

    private List<String> mapLanguages(User user) {
        return user.getLanguagesSpoken().stream()
                .map(Language::getLanguage)
                .collect(Collectors.toList());
    }
}