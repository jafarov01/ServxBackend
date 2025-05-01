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
import com.servx.servx.util.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ServiceProfileService {

    private final ServiceProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final ServiceAreaRepository areaRepository;

    @Value("${app.base-url}")
    private String appBaseUrl;

    public List<ServiceProfileDTO> getServicesByCategoryAndSubcategory(Long categoryId, Long subcategoryId) {
        return profileRepository.findByCategory_IdAndServiceArea_Id(categoryId, subcategoryId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ServiceProfileDTO createServiceProfile(Long userId, CreateServiceProfileRequestDTO request) {
        User user = getUser(userId);
        ServiceCategory category = getCategory(request.getCategoryId());
        ServiceArea serviceArea = getValidServiceArea(category, request.getServiceAreaId());

        checkForDuplicateProfile(user, category, serviceArea);

        ServiceProfile profile = buildAndSaveProfile(user, category, serviceArea, request);
        return mapToDto(profile);
    }

    public List<ServiceProfileDTO> createBulkServices(Long userId, BulkServiceProfileRequestDTO request) {
        User user = getUser(userId);
        ServiceCategory category = getCategory(request.getCategoryId());
        List<ServiceArea> validAreas = getValidServiceAreas(category, request.getServiceAreaIds());

        return validAreas.stream()
                .map(area -> createSingleProfile(user, category, area, request))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceProfileDTO> getRecommendedServices(int limit) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId;

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal() == null ||
                "anonymousUser".equals(authentication.getPrincipal().toString())) {
            log.warn("Cannot get recommendations: User is not authenticated.");
            return Collections.emptyList();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            currentUserId = ((CustomUserDetails) principal).getId();
            log.info("Fetching recommendations for user ID: {}", currentUserId);
        } else {
            log.error("Cannot get recommendations: Unexpected principal type: {}", principal.getClass());
            return Collections.emptyList();
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElse(null);

        if (currentUser == null || currentUser.getAddress() == null ||
                currentUser.getAddress().getCity() == null ||
                currentUser.getAddress().getCity().isBlank()) {
            return Collections.emptyList();
        }

        String userCity = currentUser.getAddress().getCity();

        Pageable pageable = PageRequest.of(0, limit,
                Sort.by(
                        Sort.Order.desc("rating"),
                        Sort.Order.desc("reviewCount")
                ));

        List<ServiceProfile> recommendedProfiles = profileRepository.findByProviderCityExcludingUser(userCity, currentUserId, pageable);

        return recommendedProfiles.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceProfileDTO> searchServiceProfiles(String query) {
        List<ServiceProfile> foundProfiles = profileRepository.searchProfiles(query);

        return foundProfiles.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServiceProfileDTO getServiceProfileDtoById(Long profileId) {
        ServiceProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("ServiceProfile not found with ID: " + profileId));
        return mapToDto(profile);
    }

    private ServiceProfileDTO createSingleProfile(User user, ServiceCategory category,
                                                  ServiceArea area, BulkServiceProfileRequestDTO request) {
        checkForDuplicateProfile(user, category, area);
        return mapToDto(buildAndSaveProfile(
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

    private ServiceProfileDTO mapToDto(ServiceProfile profile) {
        if (profile == null) {
            return null;
        }

        String fullPhotoUrl = null;
        if (profile.getUser() != null) {
            fullPhotoUrl = constructFullUrl(appBaseUrl, profile.getUser().getProfilePhotoUrl());
        }

        return new ServiceProfileDTO(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getFirstName() + " " + profile.getUser().getLastName(),
                profile.getCategory().getName(),
                profile.getServiceArea().getName(),
                profile.getWorkExperience(),
                profile.getPrice(),
                (profile.getReviewCount() != null && profile.getReviewCount() > 0 && profile.getRating() != null)
                        ? profile.getRating() / profile.getReviewCount()
                        : 0.0,
                profile.getReviewCount() != null ? profile.getReviewCount() : 0,
                fullPhotoUrl
        );
    }

    private String constructFullUrl(String baseUrl, String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        if (path.toLowerCase().startsWith("http://") || path.toLowerCase().startsWith("https://")) {
            return path;
        }
        String cleanBaseUrl = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl.replaceAll("/$", "") : "";
        String cleanPath = path.startsWith("/") ? path : "/" + path;
        return cleanBaseUrl.isEmpty() ? null : cleanBaseUrl + cleanPath;
    }
}