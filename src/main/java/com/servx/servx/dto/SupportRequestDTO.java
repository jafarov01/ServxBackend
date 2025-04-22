package com.servx.servx.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupportRequestDTO {

    @NotBlank(message = "Support message cannot be empty.")
    private String message;

}
