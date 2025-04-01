package com.servx.servx.dto;

import com.servx.servx.entity.ServiceProfile;
import lombok.Getter;

@Getter
public class ServiceProfileDTO {
    private Long id;
    private String providerName;
    private String categoryName;
    private String subcategoryName;
    private String workExperience;
    private Double price;
    private Double rating;
    private Integer reviewCount;

    public ServiceProfileDTO(ServiceProfile service) {
        this.id = service.getId();
        this.providerName = service.getUser().getFirstName() + " " + service.getUser().getLastName();
        this.categoryName = service.getCategory().getName();
        this.subcategoryName = service.getServiceArea().getName();
        this.workExperience = service.getWorkExperience();
        this.price = service.getPrice();
        this.rating = service.getRating();
        this.reviewCount = service.getReviewCount();
    }
}