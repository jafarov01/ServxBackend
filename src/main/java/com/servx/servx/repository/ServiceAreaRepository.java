package com.servx.servx.repository;

import com.servx.servx.entity.ServiceArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceAreaRepository extends JpaRepository<ServiceArea, Long> {

    // Find areas by category ID with JPQL for explicit control
    @Query("SELECT sa FROM ServiceArea sa WHERE sa.category.id = :categoryId")
    List<ServiceArea> findByCategoryId(@Param("categoryId") Long categoryId);
}
