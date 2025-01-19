package com.servx.servx.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;


import java.util.List;

@Setter
@Getter
public class RegisterRequestDTO {
    @NotBlank(message = "First name is mandatory")
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    private String lastName;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is mandatory")
    private String password;

    @NotBlank(message = "Phone number is mandatory")
    private String phoneNumber;

    @Size(min = 1, message = "At least one language must be provided")
    private List<String> languagesSpoken;

    @NotNull(message = "Address is mandatory")
    private AddressDTO address;

    @NotBlank(message = "Role is mandatory")
    @Pattern(regexp = "SERVICE_SEEKER|SERVICE_PROVIDER", message = "Role must be either 'ServiceSeeker' or 'ServiceProvider'")
    private String role;

    @Getter
    @Setter
    public static class AddressDTO {
        @NotBlank(message = "City is mandatory")
        private String city;

        @NotBlank(message = "Country is mandatory")
        private String country;

        @NotBlank(message = "Zip code is mandatory")
        private String zipCode;

        @NotBlank(message = "Address line is mandatory")
        private String addressLine;
    }
}
