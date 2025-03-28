package com.servx.servx.repository;

import com.servx.servx.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    // Find all services by a specific user (user_id)
    List<Service> findByUserId(Long userId);

    // Find services by service category (service_category_id)
    List<Service> findByServiceCategoryId(Long categoryId);

    // Find services by service area (service_area_id)
    List<Service> findByServiceAreaId(Long areaId);

    // Check if a service exists for a user (useful for validation)
    boolean existsByUserIdAndServiceCategoryIdAndServiceAreaId(
            Long userId,
            Long categoryId,
            Long areaId
    );
}