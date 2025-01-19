package com.servx.servx.repository;

import com.servx.servx.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    List<Profile> findByUserId(Long userId);
    boolean existsByUserIdAndServiceCategoryId(Long userId, Long serviceCategoryId);
}
