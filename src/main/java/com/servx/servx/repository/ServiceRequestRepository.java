package com.servx.servx.repository;

import com.servx.servx.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByProviderIdOrderByCreatedAtDesc(Long providerId);
    List<ServiceRequest> findBySeekerIdOrderByCreatedAtDesc(Long seekerId);
}
