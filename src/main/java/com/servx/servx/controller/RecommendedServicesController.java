package com.servx.servx.controller;

import com.servx.servx.dto.ServiceProfileDTO;
import com.servx.servx.service.ServiceData.ServiceProfileService;
import com.servx.servx.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/service-offers")
@Slf4j
@RequiredArgsConstructor
public class RecommendedServicesController {

    private final ServiceProfileService profileService;

    @GetMapping("/recommended")
    public ResponseEntity<List<ServiceProfileDTO>> getRecommendedServiceOffers() {
        final int RECOMMENDATION_LIMIT = 5;

        List<ServiceProfileDTO> recommendations = profileService.getRecommendedServices(RECOMMENDATION_LIMIT);
        log.info("Controller called.");
        log.info("Result: ", recommendations.toString());
        return ResponseEntity.ok(recommendations);
    }
}