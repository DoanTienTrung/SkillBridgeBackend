package com.skillbridge.skillbridge_backend.repository;

import com.skillbridge.skillbridge_backend.entity.UserLessonProgress;
import com.skillbridge.skillbridge_backend.entity.Question;
import com.skillbridge.skillbridge_backend.entity.ListeningLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    // ===== ANALYTICS METHODS =====

    /**
     * Tìm progress theo lesson ID và type
     */
    List<UserLessonProgress> findByLessonIdAndLessonType(Long lessonId, Question.LessonType lessonType);

    /**
     * Tìm progress theo user, sắp xếp theo thời gian tạo mới nhất
     */
    List<UserLessonProgress> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Đếm tổng số lesson completions
     */
    @Query("SELECT COUNT(p) FROM UserLessonProgress p WHERE p.isCompleted = :isCompleted")
    Integer countByIsCompleted(@Param("isCompleted") Boolean isCompleted);

    /**
     * Tính tổng thời gian học của tất cả users
     */
    @Query("SELECT COALESCE(SUM(p.timeSpentSeconds), 0) FROM UserLessonProgress p")
    Integer sumTimeSpentSeconds();

    /**
     * Đếm unique users active trong khoảng thời gian
     */
    @Query("SELECT COUNT(DISTINCT p.user.id) FROM UserLessonProgress p WHERE p.createdAt BETWEEN :start AND :end")
    Integer countDistinctUsersByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Đếm lessons completed trong khoảng thời gian
     */
    @Query("SELECT COUNT(p) FROM UserLessonProgress p WHERE p.completedAt BETWEEN :start AND :end AND p.isCompleted = :isCompleted")
    Integer countByCompletedAtBetweenAndIsCompleted(@Param("start") LocalDateTime start, 
                                                    @Param("end") LocalDateTime end, 
                                                    @Param("isCompleted") Boolean isCompleted);

    /**
     * Tính tổng thời gian học trong khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(p.timeSpentSeconds), 0) FROM UserLessonProgress p WHERE p.createdAt BETWEEN :start AND :end")
    Integer sumTimeSpentByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Kiểm tra user có hoạt động trong khoảng thời gian không
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM UserLessonProgress p WHERE p.user.id = :userId AND p.createdAt BETWEEN :start AND :end")
    Boolean existsByUserIdAndCreatedAtBetween(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Đếm lessons completed của user trong khoảng thời gian
     */
    @Query("SELECT COUNT(p) FROM UserLessonProgress p WHERE p.user.id = :userId AND p.completedAt BETWEEN :start AND :end AND p.isCompleted = :isCompleted")
    Integer countByUserIdAndCompletedAtBetweenAndIsCompleted(@Param("userId") Long userId, 
                                                             @Param("start") LocalDateTime start, 
                                                             @Param("end") LocalDateTime end, 
                                                             @Param("isCompleted") Boolean isCompleted);

    /**
     * Tính tổng thời gian học của user trong khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(p.timeSpentSeconds), 0) FROM UserLessonProgress p WHERE p.user.id = :userId AND p.createdAt BETWEEN :start AND :end")
    Integer sumTimeSpentByUserAndDateRange(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Đếm lessons completed của user theo level
     */
    @Query(value = "SELECT COUNT(DISTINCT ulp.lesson_id) FROM user_lesson_progress ulp " +
                   "LEFT JOIN listening_lessons ll ON (ulp.lesson_id = ll.id AND ulp.lesson_type = 'LISTENING') " +
                   "LEFT JOIN reading_lessons rl ON (ulp.lesson_id = rl.id AND ulp.lesson_type = 'READING') " +
                   "WHERE ulp.user_id = :userId AND ulp.is_completed = true " +
                   "AND (ll.level = :level OR rl.level = :level)", nativeQuery = true)
    Integer countCompletedLessonsByUserAndLevel(@Param("userId") Long userId, @Param("level") String level);

    /**
     * Tính điểm trung bình của user theo level
     */
    @Query(value = "SELECT AVG(ulp.score) FROM user_lesson_progress ulp " +
                   "LEFT JOIN listening_lessons ll ON (ulp.lesson_id = ll.id AND ulp.lesson_type = 'LISTENING') " +
                   "LEFT JOIN reading_lessons rl ON (ulp.lesson_id = rl.id AND ulp.lesson_type = 'READING') " +
                   "WHERE ulp.user_id = :userId AND ulp.is_completed = true AND ulp.score IS NOT NULL " +
                   "AND (ll.level = :level OR rl.level = :level)", nativeQuery = true)
    BigDecimal getAverageScoreByUserAndLevel(@Param("userId") Long userId, @Param("level") String level);

    // ===== ADDITIONAL ANALYTICS METHODS =====

    /**
     * Lấy danh sách user IDs có hoạt động trong khoảng thời gian
     */
    @Query("SELECT DISTINCT p.user.id FROM UserLessonProgress p WHERE p.createdAt BETWEEN :start AND :end")
    List<Long> findUserIdsWithActivityBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Đếm lessons completed trong khoảng thời gian
     */
    @Query("SELECT COUNT(p) FROM UserLessonProgress p WHERE p.completedAt BETWEEN :start AND :end AND p.isCompleted = true")
    Integer countByCompletedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Tính điểm trung bình trong khoảng thời gian
     */
    @Query("SELECT AVG(p.score) FROM UserLessonProgress p WHERE p.completedAt BETWEEN :start AND :end AND p.isCompleted = true AND p.score IS NOT NULL")
    Double findAverageScoreByCompletedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Đếm lessons completed theo status
     */
    Integer countByIsCompletedTrue();

    /**
     * Đếm tổng số progress records
     */
    @Query("SELECT COUNT(p) FROM UserLessonProgress p")
    Long countAll();
}
