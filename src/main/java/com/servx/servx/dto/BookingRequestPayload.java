package com.servx.servx.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestPayload {

    private Instant agreedDateTime;

    private String serviceRequestDetailsText;

    private Double priceMin;

    private Double priceMax;

    private String notes;
}