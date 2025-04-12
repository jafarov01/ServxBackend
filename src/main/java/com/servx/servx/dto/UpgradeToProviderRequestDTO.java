package com.servx.servx.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpgradeToProviderRequestDTO {
    @NotBlank(message = "Education is required")
    private String education;
}
