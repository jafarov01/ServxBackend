package com.servx.servx.controller;

import com.servx.servx.dto.ServiceProfileDTO;
import com.servx.servx.service.ServiceData.ServiceProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories/{categoryId}/subcategories/{subcategoryId}/service-offers")
@RequiredArgsConstructor
public class ServiceProfileController {
    private final ServiceProfileService profileService;

    @GetMapping
    public ResponseEntity<List<ServiceProfileDTO>> getCategoryServiceOffers(
            @PathVariable Long categoryId,
            @PathVariable Long subcategoryId) {
        return ResponseEntity.ok(profileService.getServicesByCategoryAndSubcategory(categoryId, subcategoryId));
    }
}
