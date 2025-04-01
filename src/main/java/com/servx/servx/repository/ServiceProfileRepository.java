package com.servx.servx.repository;

import com.servx.servx.entity.ServiceProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceProfileRepository extends JpaRepository<ServiceProfile, Long> {

    // Find by both category and area IDs (Exact match)
    List<ServiceProfile> findByCategory_IdAndServiceArea_Id(
            @Param("categoryId") Long categoryId,
            @Param("serviceAreaId") Long serviceAreaId
    );

    // Find all profiles for a category
    List<ServiceProfile> findByCategory_Id(@Param("categoryId") Long categoryId);
}