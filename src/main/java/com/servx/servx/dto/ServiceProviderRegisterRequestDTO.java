package com.servx.servx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ServiceProviderRegisterRequestDTO extends RegisterRequestDTO {
    @NotBlank(message = "Education is mandatory")
    private String education;

    @NotEmpty(message = "At least one profile is required")
    private List<ProfileDTO> profiles;

    @Getter
    @Setter
    @Builder
    public static class ProfileDTO {
        @NotNull(message = "Service category ID is mandatory")
        private Long serviceCategoryId;

        @NotEmpty(message = "At least one service area ID is required")
        private List<Long> serviceAreaIds;

        @NotBlank(message = "Work experience is mandatory")
        private String workExperience;
    }
}
