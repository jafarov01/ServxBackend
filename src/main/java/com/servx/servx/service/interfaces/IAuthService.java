package com.servx.servx.service.interfaces;

import com.servx.servx.dto.*;
import org.springframework.http.ResponseEntity;

public interface IAuthService {
    UserResponseDTO registerServiceSeeker(RegisterRequestDTO request);
    UserResponseDTO registerServiceProvider(ServiceProviderRegisterRequestDTO request);

    AuthResponseDTO login(LoginRequestDTO loginRequest);


    ResponseEntity<String> verifyEmail(String token);
}
