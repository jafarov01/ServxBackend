package com.servx.servx.dto;

import com.servx.servx.entity.ServiceProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServiceProfileDTO {
    private final Long id;
    private final Long providerId;
    private final String providerName;
    private final String categoryName;
    private final String subcategoryName;
    private final String workExperience;
    private final Double price;
    private final Double rating;
    private final Integer reviewCount;
    private final String profilePhotoUrl;
    // Updated Constructor
    public ServiceProfileDTO(ServiceProfile service) {
        this.id = service.getId();
        this.providerId = service.getUser().getId();
        this.providerName = service.getUser().getFirstName() + " " + service.getUser().getLastName();
        this.categoryName = service.getCategory().getName();
        this.subcategoryName = service.getServiceArea().getName();
        this.workExperience = service.getWorkExperience();
        this.price = service.getPrice();
        this.rating = (service.getReviewCount() != null && service.getReviewCount() > 0 && service.getRating() != null)
                ? service.getRating() / service.getReviewCount() : 0.0;
        this.reviewCount = service.getReviewCount() != null ? service.getReviewCount() : 0;
        this.profilePhotoUrl = service.getUser().getProfilePhotoUrl();
    }
}