package com.servx.servx.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BulkServiceProfileRequestDTO {
    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotEmpty(message = "At least one service area must be selected")
    private List<Long> serviceAreaIds;

    @NotBlank(message = "Work experience is required")
    @Size(min = 10, max = 500, message = "Work experience must be 10-500 characters")
    private String workExperience;

    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    private Double price;
}
