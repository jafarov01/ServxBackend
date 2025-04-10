package com.servx.servx.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Setter
@Getter
@Data
public class UpdateUserRequestDTO {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private AddressUpdateDTO address;

    @Builder
    @Setter
    @Getter
    @Data
    public static class AddressUpdateDTO {
        private String addressLine;
        private String city;
        private String zipCode;
        private String country;
    }
}