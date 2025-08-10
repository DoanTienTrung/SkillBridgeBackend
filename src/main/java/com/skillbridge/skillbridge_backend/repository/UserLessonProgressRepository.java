package com.skillbridge.skillbridge_backend.repository;

import com.skillbridge.skillbridge_backend.entity.UserLessonProgress;
import com.skillbridge.skillbridge_backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLessonProgressRepository extends JpaRepository<UserLessonProgress, Long> {

    /**
     * Tìm tiến độ của user
     */
    List<UserLessonProgress> findByUserId(Long userId);

    /**
     * Tìm tiến độ của user cho lesson cụ thể
     */
    Optional<UserLessonProgress> findByUserIdAndLessonIdAndLessonType(Long userId,
                                                                      Long lessonId,
                                                                      Question.LessonType lessonType);

    /**
     * Tìm các bài đã hoàn thành của user
     */
    List<UserLessonProgress> findByUserIdAndIsCompleted(Long userId, Boolean isCompleted);

    /**
     * Tìm tiến độ theo lesson type
     */
    List<UserLessonProgress> findByUserIdAndLessonType(Long userId, Question.LessonType lessonType);

    /**
     * Custom queries cho thống kê
     */
    @Query("SELECT COUNT(p) FROM UserLessonProgress p WHERE p.user.id = :userId AND p.isCompleted = true")
    Long countCompletedLessonsByUser(@Param("userId") Long userId);

    @Query("SELECT AVG(p.score) FROM UserLessonProgress p WHERE p.user.id = :userId AND p.score IS NOT NULL")
    Double getAverageScoreByUser(@Param("userId") Long userId);

    @Query("SELECT SUM(p.timeSpentSeconds) FROM UserLessonProgress p WHERE p.user.id = :userId")
    Long getTotalTimeSpentByUser(@Param("userId") Long userId);
}