package com.servx.servx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfilePhotoResponseDTO {
    @JsonProperty("url")
    private String profilePhotoUrl;

    public ProfilePhotoResponseDTO(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }
}