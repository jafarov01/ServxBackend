package com.servx.servx.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponseDTO {
    private String addressLine;
    private String city;
    private String zipCode;
    private String country;
}