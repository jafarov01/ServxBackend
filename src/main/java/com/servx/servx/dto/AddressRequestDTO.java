package com.servx.servx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequestDTO {
    @NotBlank
    private String addressLine;

    @NotBlank
    private String city;

    @NotBlank
    private String zipCode;

    @NotBlank
    @Size(min = 3, max = 3)
    private String country;
}