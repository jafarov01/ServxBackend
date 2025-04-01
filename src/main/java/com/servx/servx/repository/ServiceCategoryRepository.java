package com.servx.servx.repository;

import com.servx.servx.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {

    // Built-in methods:
    // findAll() - already exists
    // findById() - already exists
}
