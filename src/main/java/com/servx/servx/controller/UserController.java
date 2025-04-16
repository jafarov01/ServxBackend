package com.servx.servx.controller;

import com.servx.servx.dto.*;
import com.servx.servx.service.UserService;
import com.servx.servx.util.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtUtils jwtUtils; // Inject the JwtUtils service

    // upgrade to service provider endpoint
    @PostMapping("/me/upgrade-to-provider")
    public ResponseEntity<UserResponseDTO> upgradeToProvider(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody UpgradeToProviderRequestDTO request) {

        String token = authorizationHeader.replace("Bearer ", "");
        Long userId = jwtUtils.extractUserId(token);

        UserResponseDTO updatedUser = userService.upgradeToProvider(userId, request.getEducation());
        return ResponseEntity.ok(updatedUser);
    }

    // Get user details
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getUserDetails(@RequestHeader("Authorization") String authorizationHeader) {
        // Extract userId from the JWT token
        String token = authorizationHeader.replace("Bearer ", "");
        Long userId = jwtUtils.extractUserId(token); // Extract userId from JWT token

        UserResponseDTO userDetails = userService.getUserDetails(userId);
        return ResponseEntity.ok(userDetails);
    }

    // Update profile photo
    @PutMapping("/me/photo")
    public ResponseEntity<ProfilePhotoResponseDTO> updateProfilePhoto(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam("file") MultipartFile file) {

        // Extract userId from the JWT token
        String token = authorizationHeader.replace("Bearer ", "");
        Long userId = jwtUtils.extractUserId(token);

        String photoUrl = userService.updateProfilePhoto(userId, file);
        return ResponseEntity.ok(new ProfilePhotoResponseDTO(photoUrl));
    }

    // Delete profile photo
    @DeleteMapping("/me/photo")
    public ResponseEntity<DeletePhotoResponseDTO> deleteProfilePhoto(@RequestHeader("Authorization") String authorizationHeader) {
        // Extract userId from the JWT token
        String token = authorizationHeader.replace("Bearer ", "");
        Long userId = jwtUtils.extractUserId(token);

        DeletePhotoResponseDTO response = userService.deleteProfilePhoto(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateUserDetails(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody UpdateUserRequestDTO updateRequest) {

        // Extract userId from JWT
        String token = authorizationHeader.replace("Bearer ", "");
        Long userId = jwtUtils.extractUserId(token);

        // Delegate to service
        UserResponseDTO updatedUser = userService.updateUserDetails(userId, updateRequest);
        return ResponseEntity.ok(updatedUser);
    }
}