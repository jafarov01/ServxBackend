package com.servx.servx.service.ServiceData;

import com.servx.servx.dto.ServiceAreaDTO;
import com.servx.servx.repository.ServiceAreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceAreaService {
    private final ServiceAreaRepository areaRepository;

    public List<ServiceAreaDTO> getSubcategories(Long categoryId) {
        return areaRepository.findByCategoryId(categoryId).stream()
                .map(ServiceAreaDTO::new)
                .collect(Collectors.toList());
    }
}