package com.servx.servx.repository;

import com.servx.servx.entity.ServiceArea;
import com.servx.servx.entity.ServiceCategory;
import com.servx.servx.entity.ServiceProfile;
import com.servx.servx.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceProfileRepository extends JpaRepository<ServiceProfile, Long> {

    // Existing methods
    List<ServiceProfile> findByCategory_IdAndServiceArea_Id(
            @Param("categoryId") Long categoryId,
            @Param("serviceAreaId") Long serviceAreaId
    );

    List<ServiceProfile> findByCategory_Id(@Param("categoryId") Long categoryId);

    // New method for duplicate check
    boolean existsByUserAndCategoryAndServiceArea(
            User user,
            ServiceCategory category,
            ServiceArea serviceArea
    );

    // Optional: Find all profiles for a user
    List<ServiceProfile> findByUser(User user);
}