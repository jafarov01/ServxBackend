package com.servx.servx.repository;

import com.servx.servx.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Long> {
    List<Language> findByUserId(Long userId);
    boolean existsByUserIdAndLanguage(Long userId, String language);
}
