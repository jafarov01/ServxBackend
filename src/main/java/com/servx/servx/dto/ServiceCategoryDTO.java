package com.servx.servx.dto;


import com.servx.servx.entity.ServiceCategory;
import lombok.Getter;

@Getter
public class ServiceCategoryDTO {
    private Long id;
    private String name;

    public ServiceCategoryDTO(ServiceCategory category) {
        this.id = category.getId();
        this.name = category.getName();
    }
}