package com.servx.servx.service;

import com.servx.servx.dto.AddressResponseDTO;
import com.servx.servx.dto.DeletePhotoResponseDTO;
import com.servx.servx.dto.UpdateUserRequestDTO;
import com.servx.servx.dto.UserResponseDTO;
import com.servx.servx.entity.Address;
import com.servx.servx.entity.Language;
import com.servx.servx.entity.User;
import com.servx.servx.enums.Role;
import com.servx.servx.exception.UserNotFoundException;
import com.servx.servx.repository.LanguageRepository;
import com.servx.servx.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    // Fetch user details without explicitly passing userId
    public UserResponseDTO getUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        return mapToUserResponseDTO(user);
    }

    // Update profile photo and return its URL
    @Transactional
    public String updateProfilePhoto(Long userId, MultipartFile file) {
        // Validate user existence
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Generate file name
        String fileName = "profile-" + userId + "." + getFileExtension(file);
        Path filePath = Paths.get(uploadDir, fileName);

        try {
            // Create directory if not exists
            Files.createDirectories(filePath.getParent());

            // Save the file
            file.transferTo(filePath.toFile());

            // Update user's profile photo URL
            String photoUrl = "/uploads/" + fileName;
            user.setProfilePhotoUrl(photoUrl);
            userRepository.save(user);

            return fileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    // upgrade to service provider
    @Transactional
    public UserResponseDTO upgradeToProvider(Long userId, String education) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getRole() == Role.SERVICE_PROVIDER) {
            throw new IllegalStateException("User is already a Service Provider");
        }

        if (education == null || education.isBlank()) {
            throw new IllegalArgumentException("Education cannot be empty");
        }

        user.setRole(Role.SERVICE_PROVIDER);
        user.setEducation(education);
        User savedUser = userRepository.save(user);

        return mapToUserResponseDTO(savedUser);
    }

    // Delete profile photo
    @Transactional
    public DeletePhotoResponseDTO deleteProfilePhoto(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Delete the file from server
        String photoUrl = user.getProfilePhotoUrl();
        File file = new File(uploadDir + File.separator + photoUrl);
        if (file.exists()) {
            boolean delete = file.delete();
        }

        // Set profile photo URL to default
        user.setProfilePhotoUrl("/images/default-profile.jpg");
        userRepository.save(user);

        return new DeletePhotoResponseDTO(true, "Profile photo deleted successfully.");
    }

    @Transactional
    public UserResponseDTO updateUserDetails(Long userId, UpdateUserRequestDTO updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Update basic fields
        user.setFirstName(updateRequest.getFirstName());
        user.setLastName(updateRequest.getLastName());
        user.setPhoneNumber(updateRequest.getPhoneNumber());

        // Update address (assuming Address is a nested entity in User)
        Address address = user.getAddress();
        address.setAddressLine(updateRequest.getAddress().getAddressLine());
        address.setCity(updateRequest.getAddress().getCity());
        address.setZipCode(updateRequest.getAddress().getZipCode());
        address.setCountry(updateRequest.getAddress().getCountry());

        // Save and return
        return mapToUserResponseDTO(userRepository.save(user));
    }

    // Helper method to get file extension
    private String getFileExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    // Map User to UserResponseDTO
    private UserResponseDTO mapToUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .languagesSpoken(user.getLanguagesSpoken().stream()
                        .map(Language::getLanguage)  // Map each Language entity to its language code (String)
                        .collect(Collectors.toList()))  // Collect into List<String>
                .address(mapToAddressDTO(user.getAddress()))  // Map address
                .education(user.getEducation())
                .role(user.getRole().name())  // Convert role to string
                .build();
    }

    private AddressResponseDTO mapToAddressDTO(Address address) {
        return AddressResponseDTO.builder()
                .city(address.getCity())
                .country(address.getCountry())
                .zipCode(address.getZipCode())
                .addressLine(address.getAddressLine())
                .build();
    }
}