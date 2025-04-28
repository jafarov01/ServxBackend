package com.servx.servx.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateServiceProfileRequestDTO {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Service area ID is required")
    private Long serviceAreaId;

    @NotBlank(message = "Work experience is required")
    @Size(min = 10, max = 500, message = "Work experience must be 10-500 characters")
    private String workExperience;

    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    private Double price;

    public CreateServiceProfileRequestDTO(Long categoryId, Long serviceAreaId, String workExperience, Double price) {
        this.categoryId = categoryId;
        this.serviceAreaId = serviceAreaId;
        this.workExperience = workExperience;
        this.price = price;
    }
}
