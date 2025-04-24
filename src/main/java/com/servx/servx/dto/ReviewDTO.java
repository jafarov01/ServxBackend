package com.servx.servx.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ReviewDTO {
    private Long id;
    private Double rating;
    private String comment;
    private Instant createdAt;
    private String reviewerName;
    private String reviewerFirstName;
    private String reviewerLastName;
    private String reviewerProfilePhotoUrl;
}
