package com.servx.servx.controller;

import com.servx.servx.dto.ServiceCategoryDTO;
import com.servx.servx.service.ServiceData.ServiceCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class ServiceCategoryController {
    private final ServiceCategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<ServiceCategoryDTO>> getAllServiceCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
}