package com.skillbridge.skillbridge_backend.repository;

import com.skillbridge.skillbridge_backend.entity.UserVocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserVocabularyRepository extends JpaRepository<UserVocabulary, Long> {

    /**
     * Tìm từ vựng của user
     */
    List<UserVocabulary> findByUserId(Long userId);
    
    /**
     * Tìm từ vựng của user sắp xếp theo thời gian tạo
     */
    List<UserVocabulary> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * Tìm từ vựng theo user và từ
     */
    @Query("SELECT uv FROM UserVocabulary uv JOIN uv.vocabulary v WHERE uv.user.id = :userId AND LOWER(v.word) = LOWER(:word)")
    Optional<UserVocabulary> findByUserIdAndWord(@Param("userId") Long userId, @Param("word") String word);

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

    // ===== ANALYTICS METHODS =====

    /**
     * Đếm vocabularies đã học theo trạng thái
     */
    @Query("SELECT COUNT(uv) FROM UserVocabulary uv WHERE uv.isLearned = :isLearned")
    Integer countByIsLearned(@Param("isLearned") Boolean isLearned);

    /**
     * Đếm vocabularies đã học của user
     */
    @Query("SELECT COUNT(uv) FROM UserVocabulary uv WHERE uv.user.id = :userId AND uv.isLearned = :isLearned")
    Integer countByUserIdAndIsLearned(@Param("userId") Long userId, @Param("isLearned") Boolean isLearned);

    /**
     * Đếm vocabularies học trong khoảng thời gian
     */
    @Query("SELECT COUNT(uv) FROM UserVocabulary uv WHERE uv.createdAt BETWEEN :start AND :end AND uv.isLearned = :isLearned")
    Integer countByCreatedAtBetweenAndIsLearned(@Param("start") LocalDateTime start, 
                                                @Param("end") LocalDateTime end, 
                                                @Param("isLearned") Boolean isLearned);

    /**
     * Đếm vocabularies học của user trong khoảng thời gian
     */
    @Query("SELECT COUNT(uv) FROM UserVocabulary uv WHERE uv.user.id = :userId AND uv.createdAt BETWEEN :start AND :end AND uv.isLearned = :isLearned")
    Integer countByUserIdAndCreatedAtBetweenAndIsLearned(@Param("userId") Long userId, 
                                                         @Param("start") LocalDateTime start, 
                                                         @Param("end") LocalDateTime end, 
                                                         @Param("isLearned") Boolean isLearned);

    /**
     * Đếm số từ vựng đã học của user (shortcut method)
     */
    Integer countByUserIdAndIsLearnedTrue(Long userId);
}