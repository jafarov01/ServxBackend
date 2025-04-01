package com.servx.servx.service.ServiceData;

import com.servx.servx.dto.ServiceProfileDTO;
import com.servx.servx.repository.ServiceProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ServiceProfileService {
    private final ServiceProfileRepository profileRepository;

    public List<ServiceProfileDTO> getServicesByCategoryAndSubcategory(Long categoryId, Long subcategoryId) {
        return profileRepository.findByCategory_IdAndServiceArea_Id(categoryId, subcategoryId).stream()
                .map(ServiceProfileDTO::new)
                .collect(Collectors.toList());
    }
}
