package com.servx.servx.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeletePhotoResponseDTO {
    private boolean success;
    private String message;

    public DeletePhotoResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}