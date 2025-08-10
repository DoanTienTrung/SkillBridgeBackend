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
}