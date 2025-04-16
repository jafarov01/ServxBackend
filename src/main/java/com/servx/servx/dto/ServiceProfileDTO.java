package com.servx.servx.dto;

import com.servx.servx.entity.ServiceProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServiceProfileDTO {
    private final Long id;
    private final String providerName;
    private final String categoryName;
    private final String subcategoryName;
    private final String workExperience;
    private final Double price;
    private final Double rating;
    private final Integer reviewCount;

    public ServiceProfileDTO(ServiceProfile service) {
        this(
                service.getId(),
                service.getUser().getFirstName() + " " + service.getUser().getLastName(),
                service.getCategory().getName(),
                service.getServiceArea().getName(),
                service.getWorkExperience(),
                service.getPrice(),
                service.getReviewCount() > 0 ?
                        service.getRating() / service.getReviewCount() : 0.0,
                service.getReviewCount()
        );
    }
}