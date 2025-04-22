package com.servx.servx.service.ServiceData;

import com.servx.servx.dto.BulkServiceProfileRequestDTO;
import com.servx.servx.dto.CreateServiceProfileRequestDTO;
import com.servx.servx.dto.ServiceProfileDTO;
import com.servx.servx.entity.ServiceArea;
import com.servx.servx.entity.ServiceCategory;
import com.servx.servx.entity.ServiceProfile;
import com.servx.servx.entity.User;
import com.servx.servx.exception.*;
import com.servx.servx.repository.ServiceAreaRepository;
import com.servx.servx.repository.ServiceCategoryRepository;
import com.servx.servx.repository.ServiceProfileRepository;
import com.servx.servx.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ServiceProfileService {
    private final ServiceProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final ServiceAreaRepository areaRepository;

    public List<ServiceProfileDTO> getServicesByCategoryAndSubcategory(Long categoryId, Long subcategoryId) {
        return profileRepository.findByCategory_IdAndServiceArea_Id(categoryId, subcategoryId)
                .stream()
                .map(ServiceProfileDTO::new)
                .collect(Collectors.toList());
    }

    public ServiceProfileDTO createServiceProfile(Long userId, CreateServiceProfileRequestDTO request) {
        User user = getUser(userId);
        ServiceCategory category = getCategory(request.getCategoryId());
        ServiceArea serviceArea = getValidServiceArea(category, request.getServiceAreaId());

        checkForDuplicateProfile(user, category, serviceArea);

        ServiceProfile profile = buildAndSaveProfile(user, category, serviceArea, request);
        return new ServiceProfileDTO(profile);
    }

    public List<ServiceProfileDTO> createBulkServices(Long userId, BulkServiceProfileRequestDTO request) {
        User user = getUser(userId);
        ServiceCategory category = getCategory(request.getCategoryId());
        List<ServiceArea> validAreas = getValidServiceAreas(category, request.getServiceAreaIds());

        return validAreas.stream()
                .map(area -> createSingleProfile(user, category, area, request))
                .collect(Collectors.toList());
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    private ServiceCategory getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + categoryId));
    }

    private ServiceArea getValidServiceArea(ServiceCategory category, Long areaId) {
        return areaRepository.findById(areaId)
                .filter(area -> area.getCategory().equals(category))
                .orElseThrow(() -> new InvalidServiceAreaException(
                        "ServiceArea ID " + areaId + " doesn't belong to Category ID " + category.getId()));
    }

    private List<ServiceArea> getValidServiceAreas(ServiceCategory category, List<Long> areaIds) {
        List<ServiceArea> areas = areaRepository.findAllById(areaIds);

        List<Long> invalidIds = areas.stream()
                .filter(area -> !area.getCategory().equals(category))
                .map(ServiceArea::getId)
                .collect(Collectors.toList());

        if (!invalidIds.isEmpty()) {
            throw new MismatchedCategoryException("Invalid ServiceArea IDs for Category " +
                    category.getId() + ": " + invalidIds);
        }

        return areas;
    }

    private void checkForDuplicateProfile(User user, ServiceCategory category, ServiceArea area) {
        if (profileRepository.existsByUserAndCategoryAndServiceArea(user, category, area)) {
            throw new DuplicateEntryException("Service profile already exists for: " +
                    category.getName() + " - " + area.getName());
        }
    }

    private ServiceProfile buildAndSaveProfile(User user, ServiceCategory category,
                                               ServiceArea area, CreateServiceProfileRequestDTO request) {
        ServiceProfile profile = ServiceProfile.builder()
                .user(user)
                .category(category)
                .serviceArea(area)
                .workExperience(request.getWorkExperience())
                .price(request.getPrice())
                .build();

        return profileRepository.save(profile);
    }

    @Transactional(readOnly = true) // Good practice for fetch methods
    public ServiceProfileDTO getServiceProfileDtoById(Long profileId) {
        ServiceProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("ServiceProfile not found with ID: " + profileId));
        return new ServiceProfileDTO(profile);
    }

    private ServiceProfileDTO createSingleProfile(User user, ServiceCategory category,
                                                  ServiceArea area, BulkServiceProfileRequestDTO request) {
        checkForDuplicateProfile(user, category, area);
        return new ServiceProfileDTO(buildAndSaveProfile(
                user,
                category,
                area,
                new CreateServiceProfileRequestDTO(
                        category.getId(),
                        area.getId(),
                        request.getWorkExperience(),
                        request.getPrice()
                )
        ));
    }
}