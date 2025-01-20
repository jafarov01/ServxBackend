package com.servx.servx.service.interfaces;

import com.servx.servx.dto.*;

public interface IAuthService {
    UserResponseDTO registerServiceSeeker(RegisterRequestDTO request);
    UserResponseDTO registerServiceProvider(ServiceProviderRegisterRequestDTO request);
    AuthResponseDTO login(LoginRequestDTO loginRequest);
}
