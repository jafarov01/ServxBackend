package com.servx.servx.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data // Lombok annotation for getters, setters, toString, equals, hashCode
public class ForgotPasswordRequestDTO {
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;
}
