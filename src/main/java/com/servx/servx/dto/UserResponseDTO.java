package com.servx.servx.dto;

import com.servx.servx.entity.Language;
import io.micrometer.common.lang.Nullable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private List<String> languagesSpoken;
    private AddressResponseDTO address;
    private String role;
    private String profilePhotoUrl;
    @Nullable
    private String education;

}
