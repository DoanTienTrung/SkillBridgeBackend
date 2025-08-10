package com.skillbridge.skillbridge_backend.repository;

import com.skillbridge.skillbridge_backend.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}