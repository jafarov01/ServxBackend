package com.servx.servx.controller;

import com.servx.servx.dto.LoginRequestDTO;
import com.servx.servx.dto.RegisterRequestDTO;
import com.servx.servx.dto.ServiceProviderRegisterRequestDTO;
import com.servx.servx.dto.UserResponseDTO;
import com.servx.servx.service.interfaces.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
//@RequiredArgsConstructor
public class AuthController {
    private final IAuthService authService;

    // Constructor injection
    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/seeker")
    public ResponseEntity<UserResponseDTO> registerServiceSeeker(@Valid @RequestBody RegisterRequestDTO request) {
        UserResponseDTO response = authService.registerServiceSeeker(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register/provider")
    public ResponseEntity<UserResponseDTO> registerServiceProvider(@Valid @RequestBody ServiceProviderRegisterRequestDTO request) {
        UserResponseDTO response = authService.registerServiceProvider(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequestDTO request) {
        String token = authService.login(request);

        return ResponseEntity.ok(token);
    }

}
