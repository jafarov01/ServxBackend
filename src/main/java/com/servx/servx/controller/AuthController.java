package com.servx.servx.controller;

import com.servx.servx.dto.*;
import com.servx.servx.service.Auth.interfaces.IAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        UserResponseDTO response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        return authService.verifyEmail(token);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

}
