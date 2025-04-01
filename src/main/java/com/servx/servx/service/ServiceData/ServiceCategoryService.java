package com.servx.servx.service.ServiceData;

import com.servx.servx.dto.ServiceCategoryDTO;
import com.servx.servx.repository.ServiceCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceCategoryService {
    private final ServiceCategoryRepository categoryRepository;

    public List<ServiceCategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(ServiceCategoryDTO::new)
                .collect(Collectors.toList());
    }
}