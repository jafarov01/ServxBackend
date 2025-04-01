package com.servx.servx.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserDetailsResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String profilePhotoUrl;
    private String role;
    private AddressDTO address;
    private List<String> languagesSpoken;

    @Data
    @Builder
    public static class AddressDTO {
        private String city;
        private String country;
        private String zipCode;
        private String addressLine;
    }
}