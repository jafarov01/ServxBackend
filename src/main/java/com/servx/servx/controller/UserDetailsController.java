package com.servx.servx.controller;

import com.servx.servx.dto.UserDetailsResponseDTO;
import com.servx.servx.service.UserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserDetailsController {

    private final UserDetailsService userDetailsService;

    @GetMapping("/me")
    //@PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDetailsResponseDTO> getCurrentUserDetails() {
        return ResponseEntity.ok(userDetailsService.getCurrentUserDetails());
    }
}
