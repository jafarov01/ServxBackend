package com.servx.servx.dto;

import com.servx.servx.entity.ServiceArea;
import lombok.Getter;

@Getter
public class ServiceAreaDTO {
    private Long id;
    private String name;
    private Long categoryId;

    public ServiceAreaDTO(ServiceArea area) {
        this.id = area.getId();
        this.name = area.getName();
        this.categoryId = area.getCategory().getId();
    }
}
