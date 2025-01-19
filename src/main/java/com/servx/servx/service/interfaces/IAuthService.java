package com.servx.servx.service.interfaces;

import com.servx.servx.dto.LoginRequestDTO;
import com.servx.servx.dto.RegisterRequestDTO;
import com.servx.servx.dto.ServiceProviderRegisterRequestDTO;
import com.servx.servx.dto.UserResponseDTO;

public interface IAuthService {
    UserResponseDTO registerServiceSeeker(RegisterRequestDTO request);
    UserResponseDTO registerServiceProvider(ServiceProviderRegisterRequestDTO request);
    String login(LoginRequestDTO loginRequest);
}
