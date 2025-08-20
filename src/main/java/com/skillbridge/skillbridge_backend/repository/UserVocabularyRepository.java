package com.skillbridge.skillbridge_backend.repository;

import com.skillbridge.skillbridge_backend.entity.UserVocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserVocabularyRepository extends JpaRepository<UserVocabulary, Long> {

    /**
     * Tìm từ vựng của user
     */
    List<UserVocabulary> findByUserId(Long userId);

    /**
     * Tìm từ vựng đã học/chưa học
     */
    List<UserVocabulary> findByUserIdAndIsLearned(Long userId, Boolean isLearned);

    /**
     * Check user đã lưu từ này chưa
     */
    Optional<UserVocabulary> findByUserIdAndVocabularyId(Long userId, Long vocabularyId);

    boolean existsByUserIdAndVocabularyId(Long userId, Long vocabularyId);

    /**
     * Thống kê từ vựng
     */
    @Query("SELECT COUNT(uv) FROM UserVocabulary uv WHERE uv.user.id = :userId AND uv.isLearned = true")
    Long countLearnedVocabByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(uv) FROM UserVocabulary uv WHERE uv.user.id = :userId")
    Long countTotalVocabByUser(@Param("userId") Long userId);

    long countByUserId(Long userId);
}