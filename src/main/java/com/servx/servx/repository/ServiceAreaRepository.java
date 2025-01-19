package com.servx.servx.repository;

import com.servx.servx.entity.ServiceArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceAreaRepository extends JpaRepository<ServiceArea, Long> {
    List<ServiceArea> findByCategoryId(Long categoryId);
    boolean existsByCategoryIdAndName(Long categoryId, String name);
}
