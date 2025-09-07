package com.skillbridge.skillbridge_backend.repository;

import com.skillbridge.skillbridge_backend.entity.ReadingLesson;
import com.skillbridge.skillbridge_backend.entity.ListeningLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingLessonRepository extends JpaRepository<ReadingLesson, Long> {



    /**
     * Tìm bài đọc theo trạng thái
     */
    List<ReadingLesson> findByStatus(ListeningLesson.Status status);

    /**
     * Tìm bài đọc theo level
     */
    List<ReadingLesson> findByLevel(ListeningLesson.Level level);

    /**
     * Tìm bài đọc theo category
     */
    List<ReadingLesson> findByCategoryId(Long categoryId);

    /**
     * Tìm bài published theo level và category
     */
    @Query("SELECT r FROM ReadingLesson r WHERE r.status = 'PUBLISHED' " +
            "AND r.level = :level AND r.category.id = :categoryId " +
            "ORDER BY r.createdAt DESC")
    List<ReadingLesson> findPublishedByLevelAndCategory(@Param("level") ListeningLesson.Level level,
                                                        @Param("categoryId") Long categoryId);

    /**
     * Search theo title
     */
    List<ReadingLesson> findByTitleContainingIgnoreCaseAndStatus(String title,
                                                                 ListeningLesson.Status status);

    /**
     * Tìm bài theo word count range
     */
    List<ReadingLesson> findByWordCountBetweenAndStatus(Integer minWords, Integer maxWords,
                                                        ListeningLesson.Status status);

    /**
     * Bài đọc của teacher
     */
    List<ReadingLesson> findByCreatedById(Long teacherId);

    // ===== STUDENT-SPECIFIC METHODS =====
    
    /**
     * Find lesson by ID and status (for student access)
     */
    java.util.Optional<ReadingLesson> findByIdAndStatus(Long id, ListeningLesson.Status status);
    
    /**
     * Count lessons by status
     */

    
    /**
     * Find published lessons with filters for students
     */
    @Query("SELECT r FROM ReadingLesson r WHERE r.status = 'PUBLISHED' " +
           "AND (:level IS NULL OR r.level = :level) " +
           "AND (:categoryId IS NULL OR r.category.id = :categoryId) " +
           "AND (:search IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<ReadingLesson> findPublishedWithFilters(
        @Param("level") String level,
        @Param("categoryId") Long categoryId,
        @Param("search") String search
    );

    // ===== ANALYTICS METHODS =====

    /**
     * Đếm tổng số reading lessons
     */
    @Query("SELECT COUNT(r) FROM ReadingLesson r")
    Integer countAll();

    /**
     * Đếm lessons theo status
     */
    @Query("SELECT COUNT(r) FROM ReadingLesson r WHERE r.status = :status")
    Integer countByStatus(@Param("status") ListeningLesson.Status status);

    /**
     * Đếm lessons theo level
     */
    @Query("SELECT COUNT(r) FROM ReadingLesson r WHERE r.level = :level")
    Integer countByLevel(@Param("level") ListeningLesson.Level level);
}
