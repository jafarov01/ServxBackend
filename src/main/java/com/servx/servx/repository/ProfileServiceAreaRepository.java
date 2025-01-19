package com.servx.servx.repository;

import com.servx.servx.entity.ProfileServiceArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileServiceAreaRepository extends JpaRepository<ProfileServiceArea, Long> {
    List<ProfileServiceArea> findByProfileId(Long profileId);
    boolean existsByProfileIdAndServiceAreaId(Long profileId, Long serviceAreaId);
}