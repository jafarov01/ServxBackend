package com.servx.servx.util;

import com.servx.servx.dto.ReviewDTO;
import com.servx.servx.entity.Review;
import com.servx.servx.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    // Inject base URL for photo URLs
    @Value("${app.base-url}")
    private String appBaseUrl;

    public ReviewDTO toDto(Review review) {
        if (review == null) {
            return null;
        }

        User reviewer = review.getUser(); // Get the user who wrote the review
        String reviewerName = "Unknown User";
        String reviewerFirstName = "";
        String reviewerLastName = "";
        String photoUrl = null;

        if (reviewer != null) {
            reviewerFirstName = reviewer.getFirstName() != null ? reviewer.getFirstName() : "";
            reviewerLastName = reviewer.getLastName() != null ? reviewer.getLastName() : "";
            reviewerName = (reviewerFirstName + " " + reviewerLastName).trim();
            if (reviewerName.isEmpty()) {
                reviewerName = "Servx User"; // Fallback name
            }
            // Construct the full photo URL using the helper
            photoUrl = constructFullUrl(appBaseUrl, reviewer.getProfilePhotoUrl());
        }

        return ReviewDTO.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt()) // Pass Instant directly, Jackson handles formatting
                .reviewerName(reviewerName)
                .reviewerFirstName(reviewerFirstName)
                .reviewerLastName(reviewerLastName)
                .reviewerProfilePhotoUrl(photoUrl)
                .build();
    }

    // Helper to map a Page of entities to a Page of DTOs
    // Uses the built-in map function of the Page interface
    public Page<ReviewDTO> toDtoPage(Page<Review> reviewPage) {
        return reviewPage.map(this::toDto);
    }

    // Helper function to safely construct the full URL (same as in other mappers)
    private String constructFullUrl(String baseUrl, String path) {
        if (path == null || path.isBlank() || baseUrl == null || baseUrl.isBlank()) {
            return null;
        }
        if (path.toLowerCase().startsWith("http://") || path.toLowerCase().startsWith("https://")) {
            return path; // Already absolute
        }
        String cleanBaseUrl = baseUrl.replaceAll("/$", "");
        String cleanPath = path.startsWith("/") ? path : "/" + path;
        return cleanBaseUrl.isEmpty() ? null : cleanBaseUrl + cleanPath;
    }
}
