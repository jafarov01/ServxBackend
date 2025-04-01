package com.servx.servx.controller;

import com.servx.servx.dto.ServiceAreaDTO;
import com.servx.servx.service.ServiceData.ServiceAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories/{categoryId}/subcategories")
@RequiredArgsConstructor
public class ServiceAreaController {
    private final ServiceAreaService areaService;

    @GetMapping
    public ResponseEntity<List<ServiceAreaDTO>> getCategorySubcategories(
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(areaService.getSubcategories(categoryId));
    }
}