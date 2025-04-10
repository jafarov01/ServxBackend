package com.servx.servx.repository;

import com.servx.servx.entity.Language;
import org.apache.tomcat.util.http.MimeHeaders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Long> {
    List<Language> findByUserId(Long userId);
    boolean existsByUserIdAndLanguage(Long userId, String language);
    Optional<Language> findByUserIdAndLanguage(Long userId, String language);
}
