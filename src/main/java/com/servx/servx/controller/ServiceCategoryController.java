package com.servx.servx.controller;

import com.servx.servx.dto.ServiceAreaDTO;
import com.servx.servx.entity.ServiceArea;
import com.servx.servx.entity.ServiceCategory;
import com.servx.servx.service.ServiceData.ServiceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceCategoryController {
    private final ServiceCategoryService serviceCategoryService;

    @Autowired
    public ServiceCategoryController(ServiceCategoryService serviceCategoryService) {
        this.serviceCategoryService = serviceCategoryService;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<ServiceCategory>> getServiceCategories() {
        List<ServiceCategory> categories = serviceCategoryService.getAllServiceCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/areas/{categoryId}")
    public ResponseEntity<List<ServiceAreaDTO>> getServiceAreas(@PathVariable Long categoryId) {
        List<ServiceAreaDTO> areas = serviceCategoryService.getServiceAreasByCategoryId(categoryId);
        return ResponseEntity.ok(areas);
    }

}
