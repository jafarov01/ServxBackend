package com.servx.servx.controller;

import com.servx.servx.dto.ServiceProfileDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/service-offers")
public class RecommendedServicesController {

    // Placeholder endpoint for the recommended services
    @GetMapping("/recommended")
    public ResponseEntity<List<ServiceProfileDTO>> getRecommendedServices() {
        // For now, return an empty list of recommended services
        return ResponseEntity.ok(Collections.emptyList());
    }
}