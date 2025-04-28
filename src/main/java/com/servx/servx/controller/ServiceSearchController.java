package com.servx.servx.controller;

import com.servx.servx.dto.ServiceProfileDTO;
import com.servx.servx.service.ServiceData.ServiceProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceSearchController {

    private final ServiceProfileService serviceProfileService;

    @GetMapping("/search")
    public ResponseEntity<List<ServiceProfileDTO>> searchServices(
            @RequestParam(name = "q", required = true) String query) {

        String trimmedQuery = query.trim();
        if (trimmedQuery.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<ServiceProfileDTO> results = serviceProfileService.searchServiceProfiles(trimmedQuery);

        return ResponseEntity.ok(results);
    }
}