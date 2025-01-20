package com.servx.servx.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AuthResponseDTO {
    private String token;
    private String role;
}
