package com.servx.servx.controller;

import com.servx.servx.dto.DeletePhotoResponseDTO;
import com.servx.servx.dto.ProfilePhotoResponseDTO;
import com.servx.servx.dto.UpdateUserRequestDTO;
import com.servx.servx.dto.UserResponseDTO;
import com.servx.servx.service.UserService;
import com.servx.servx.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtUtils jwtUtils; // Inject the JwtUtils service

    // Get user details
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getUserDetails(@RequestHeader("Authorization") String authorizationHeader) {
        // Extract userId from the JWT token
        String token = authorizationHeader.replace("Bearer ", "");
        Long userId = jwtUtils.extractUserId(token); // Extract userId from JWT token

        UserResponseDTO userDetails = userService.getUserDetails(userId); // Pass userId to the service
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

        String photoUrl = userService.updateProfilePhoto(userId, file);  // Pass userId to the service
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