package com.servx.servx.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDTO {

    @NotNull(message = "Booking ID is required to submit a review.")
    private Long bookingId; // ID of the booking being reviewed

    @NotNull(message = "Rating is required.")
    @Min(value = 1, message = "Rating must be at least 1.")
    @Max(value = 5, message = "Rating must be at most 5.")
    private Double rating; // The star rating (e.g., 4.5, 5.0) - Use Double for flexibility or Integer if only whole stars

    private String comment; // Optional comment text
}
