package com.servx.servx.service.ServiceData;

import com.servx.servx.dto.ServiceAreaDTO;
import com.servx.servx.entity.ServiceArea;
import com.servx.servx.entity.ServiceCategory;
import com.servx.servx.repository.ServiceAreaRepository;
import com.servx.servx.repository.ServiceCategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceCategoryService {
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceAreaRepository serviceAreaRepository;

    public List<ServiceCategory> getAllServiceCategories() {
        return serviceCategoryRepository.findAll();
    }

    @Transactional
    public List<ServiceAreaDTO> getServiceAreasByCategoryId(Long categoryId) {
        // Fetch the ServiceAreas
        List<ServiceArea> areas = serviceAreaRepository.findByCategoryId(categoryId);

        // Map ServiceArea to ServiceAreaDTO
        return areas.stream().map(area ->
                ServiceAreaDTO.builder()
                        .id(area.getId())
                        .name(area.getName())
                        .categoryId(area.getCategory().getId())
                        .categoryName(area.getCategory().getName())
                        .build()
        ).collect(Collectors.toList()); // Close the stream mapping
    }
}