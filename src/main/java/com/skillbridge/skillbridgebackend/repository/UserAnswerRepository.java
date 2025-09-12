package com.skillbridge.skillbridgebackend.repository;

import com.skillbridge.skillbridgebackend.entity.UserAnswer;
import com.skillbridge.skillbridgebackend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {

    /**
     * Tìm câu trả lời của user
     */
    List<UserAnswer> findByUserId(Long userId);

    /**
     * Tìm câu trả lời cho question cụ thể
     */
    List<UserAnswer> findByQuestionId(Long questionId);

    /**
     * Tìm câu trả lời của user cho question
     */
    List<UserAnswer> findByUserIdAndQuestionId(Long userId, Long questionId);

    /**
     * Thống kê đáp án
     */
    @Query("SELECT COUNT(ua) FROM UserAnswer ua WHERE ua.user.id = :userId AND ua.isCorrect = true")
    Long countCorrectAnswersByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(ua) FROM UserAnswer ua WHERE ua.user.id = :userId")
    Long countTotalAnswersByUser(@Param("userId") Long userId);

    // ===== ANALYTICS METHODS =====

    /**
     * Đếm tổng số câu trả lời
     */
    @Query("SELECT COUNT(ua) FROM UserAnswer ua")
    Integer countAll();

    /**
     * Đếm câu trả lời trong khoảng thời gian
     */
    @Query("SELECT COUNT(ua) FROM UserAnswer ua WHERE ua.answeredAt BETWEEN :start AND :end")
    Integer countByAnsweredAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Đếm câu trả lời của user trong khoảng thời gian
     */
    @Query("SELECT COUNT(ua) FROM UserAnswer ua WHERE ua.user.id = :userId AND ua.answeredAt BETWEEN :start AND :end")
    Integer countByUserIdAndAnsweredAtBetween(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Tìm câu trả lời của user cho lessons cụ thể
     */
    @Query(value = "SELECT ua.* FROM user_answers ua " +
                   "INNER JOIN questions q ON ua.question_id = q.id " +
                   "WHERE ua.user_id = :userId " +
                   "AND ((q.lesson_type = :lessonType AND q.listening_lesson_id = :lessonId) " +
                   "OR (q.lesson_type = :lessonType AND q.reading_lesson_id = :lessonId))", nativeQuery = true)
    List<UserAnswer> findByUserIdAndQuestionLessonId(@Param("userId") Long userId, 
                                                      @Param("lessonId") Long lessonId, 
                                                      @Param("lessonType") String lessonType);

    /**
     * Tìm user answers cho list of questions
     */
    @Query("SELECT ua FROM UserAnswer ua WHERE ua.user.id = :userId AND ua.question IN :questions")
    List<UserAnswer> findByUserIdAndQuestionIn(@Param("userId") Long userId, @Param("questions") List<Question> questions);
}