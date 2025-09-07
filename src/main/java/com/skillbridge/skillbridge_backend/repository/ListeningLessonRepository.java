package com.skillbridge.skillbridge_backend.repository;

import com.skillbridge.skillbridge_backend.entity.ListeningLesson;
import com.skillbridge.skillbridge_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ListeningLessonRepository extends JpaRepository<ListeningLesson, Long> {


    // ===== QUERY BY STATUS =====

    /**
     * Tìm bài học theo trạng thái
     */
    List<ListeningLesson> findByStatus(ListeningLesson.Status status);

    /**
     * Chỉ lấy bài đã publish
     */
    List<ListeningLesson> findByStatusOrderByCreatedAtDesc(ListeningLesson.Status status);

    // ===== QUERY BY LEVEL =====

    /**
     * Tìm bài học theo level
     */
    List<ListeningLesson> findByLevel(ListeningLesson.Level level);

    /**
     * Tìm bài published theo level
     */
    List<ListeningLesson> findByLevelAndStatus(ListeningLesson.Level level,
                                               ListeningLesson.Status status);

    // ===== QUERY BY CATEGORY =====

    /**
     * Tìm bài học theo category ID
     */
    List<ListeningLesson> findByCategoryId(Long categoryId);

    /**
     * Tìm bài published theo category
     */
    @Query("SELECT l FROM ListeningLesson l WHERE l.category.id = :categoryId AND l.status = 'PUBLISHED'")
    List<ListeningLesson> findPublishedByCategoryId(@Param("categoryId") Long categoryId);

    // ===== COMPLEX QUERIES =====

    /**
     * Tìm bài published theo level và category
     */
    @Query("SELECT l FROM ListeningLesson l WHERE l.status = 'PUBLISHED' " +
            "AND l.level = :level AND l.category.id = :categoryId " +
            "ORDER BY l.createdAt DESC")
    List<ListeningLesson> findPublishedByLevelAndCategory(@Param("level") ListeningLesson.Level level,
                                                          @Param("categoryId") Long categoryId);

    /**
     * Tìm bài theo tác giả
     */
    List<ListeningLesson> findByCreatedById(Long teacherId);

    /**
     * Tìm bài trong khoảng thời gian
     */
    List<ListeningLesson> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Search bài học theo title
     */
    List<ListeningLesson> findByTitleContainingIgnoreCaseAndStatus(String title,
                                                                   ListeningLesson.Status status);

    /**
     * Đếm số bài theo level
     */
    @Query("SELECT l.level, COUNT(l) FROM ListeningLesson l WHERE l.status = 'PUBLISHED' GROUP BY l.level")
    List<Object[]> countLessonsByLevel();

    /**
     * Top 5 bài mới nhất
     */
    List<ListeningLesson> findTop5ByStatusOrderByCreatedAtDesc(ListeningLesson.Status status);

    List<ListeningLesson> findAllByOrderByCreatedAtDesc();
    List<ListeningLesson> findByCreatedByOrderByCreatedAtDesc(User createdBy);

    // ===== STUDENT-SPECIFIC METHODS =====
    
    /**
     * Find lesson by ID and status (for student access)
     */
    java.util.Optional<ListeningLesson> findByIdAndStatus(Long id, ListeningLesson.Status status);
    
    /**
     * Count lessons by status
     */
    Integer countByStatus(ListeningLesson.Status status);
    
    /**
     * Find published lessons with filters for students
     */
    @Query("SELECT l FROM ListeningLesson l WHERE l.status = 'PUBLISHED' " +
           "AND (:level IS NULL OR l.level = :level) " +
           "AND (:categoryId IS NULL OR l.category.id = :categoryId) " +
           "AND (:search IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(l.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<ListeningLesson> findPublishedWithFilters(
        @Param("level") String level,
        @Param("categoryId") Long categoryId,
        @Param("search") String search
    );

    // ===== ANALYTICS METHODS =====

    /**
     * Đếm tổng số listening lessons
     */
    @Query("SELECT COUNT(l) FROM ListeningLesson l")
    Integer countAll();

    /**
     * Đếm lessons theo status
     */
   // @Query("SELECT COUNT(l) FROM ListeningLesson l WHERE l.status = :status")
    //Integer countByStatus(@Param("status") ListeningLesson.Status status);

    /**
     * Đếm lessons theo level
     */
    @Query("SELECT COUNT(l) FROM ListeningLesson l WHERE l.level = :level")
    Integer countByLevel(@Param("level") ListeningLesson.Level level);
}