package com.servx.servx.dto;

import com.servx.servx.entity.ServiceRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestDTO {
    @NotBlank
    @Size(max = 500)
    private String description;

    @NotNull
    private ServiceRequest.SeverityLevel severity;

    @NotNull
    private Long serviceId;

    @Valid
    private AddressRequestDTO address;
}
