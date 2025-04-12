package com.servx.servx.controller;

import com.servx.servx.dto.BulkServiceProfileRequestDTO;
import com.servx.servx.dto.CreateServiceProfileRequestDTO;
import com.servx.servx.dto.ServiceProfileDTO;
import com.servx.servx.exception.MismatchedCategoryException;
import com.servx.servx.service.ServiceData.ServiceProfileService;
import com.servx.servx.util.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories/{categoryId}")
@RequiredArgsConstructor
public class ServiceProfileController {
    private final ServiceProfileService profileService;
    private final JwtUtils jwtUtils;

    @GetMapping("/subcategories/{subcategoryId}/service-offers")
    public ResponseEntity<List<ServiceProfileDTO>> getServicesBySubcategory(
            @PathVariable Long categoryId,
            @PathVariable Long subcategoryId) {
        return ResponseEntity.ok(profileService.getServicesByCategoryAndSubcategory(categoryId, subcategoryId));
    }

    @PostMapping("/subcategories/{subcategoryId}/service-offers")
    public ResponseEntity<ServiceProfileDTO> createSingleServiceProfile(
            @PathVariable Long categoryId,
            @PathVariable Long subcategoryId,
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody CreateServiceProfileRequestDTO request) {

        validatePathConsistency(categoryId, subcategoryId, request);
        Long userId = extractUserId(authorizationHeader);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.createServiceProfile(userId, request));
    }

    @PostMapping("/service-offers/bulk")
    public ResponseEntity<List<ServiceProfileDTO>> createBulkServices(
            @PathVariable Long categoryId,
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody BulkServiceProfileRequestDTO request) {

        validateCategoryConsistency(categoryId, request);
        Long userId = extractUserId(authorizationHeader);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.createBulkServices(userId, request));
    }

    private void validatePathConsistency(Long pathCategoryId, Long pathSubcategoryId,
                                         CreateServiceProfileRequestDTO request) {
        if (!pathCategoryId.equals(request.getCategoryId()) ||
                !pathSubcategoryId.equals(request.getServiceAreaId())) {
            throw new MismatchedCategoryException("Path parameters don't match request body: " +
                    "Path Category ID: " + pathCategoryId + " vs Body: " + request.getCategoryId() + ", " +
                    "Path Subcategory ID: " + pathSubcategoryId + " vs Body: " + request.getServiceAreaId());
        }
    }

    private void validateCategoryConsistency(Long pathCategoryId, BulkServiceProfileRequestDTO request) {
        if (!pathCategoryId.equals(request.getCategoryId())) {
            throw new MismatchedCategoryException("Path Category ID: " + pathCategoryId +
                    " doesn't match request body Category ID: " + request.getCategoryId());
        }
    }

    private Long extractUserId(String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        return jwtUtils.extractUserId(token);
    }
}