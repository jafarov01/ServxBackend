package com.servx.servx.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ServiceAreaDTO {
    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
}
