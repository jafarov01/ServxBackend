package com.servx.servx.repository;

import com.servx.servx.entity.ServiceArea;
import com.servx.servx.entity.ServiceCategory;
import com.servx.servx.entity.ServiceProfile;
import com.servx.servx.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceProfileRepository extends JpaRepository<ServiceProfile, Long> {

    List<ServiceProfile> findByCategory_IdAndServiceArea_Id(
            @Param("categoryId") Long categoryId,
            @Param("serviceAreaId") Long serviceAreaId
    );

    boolean existsByUserAndCategoryAndServiceArea(
            User user,
            ServiceCategory category,
            ServiceArea serviceArea
    );

    @Query("SELECT sp FROM ServiceProfile sp " +
            "JOIN sp.user u " +
            "JOIN u.address a " +
            "WHERE u.role = 'SERVICE_PROVIDER' " +
            "AND LOWER(a.city) = LOWER(:city) " +
            "AND u.id <> :excludeUserId")
    List<ServiceProfile> findByProviderCityExcludingUser(
                                                          @Param("city") String city,
                                                          @Param("excludeUserId") Long excludeUserId,
                                                          Pageable pageable
    );

    @Query("SELECT DISTINCT sp FROM ServiceProfile sp " +
            "JOIN sp.user u " +
            "JOIN u.address a " +
            "JOIN sp.category sc " +
            "JOIN sp.serviceArea sa " +
            "WHERE u.role = 'SERVICE_PROVIDER' AND (" +
            "  LOWER(a.city) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "  LOWER(a.zipCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "  LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "  LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "  LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "  LOWER(sc.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "  LOWER(sa.name) LIKE LOWER(CONCAT('%', :query, '%'))" +
            ")")
    List<ServiceProfile> searchProfiles(@Param("query") String query);
}