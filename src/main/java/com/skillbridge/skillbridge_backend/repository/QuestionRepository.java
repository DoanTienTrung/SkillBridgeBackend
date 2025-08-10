package com.skillbridge.skillbridge_backend.repository;

import com.skillbridge.skillbridge_backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * Tìm câu hỏi của bài nghe
     */
    List<Question> findByListeningLessonId(Long lessonId);

    /**
     * Tìm câu hỏi của bài đọc
     */
    List<Question> findByReadingLessonId(Long lessonId);

    /**
     * Tìm câu hỏi theo loại bài học
     */
    List<Question> findByLessonType(Question.LessonType lessonType);

    /**
     * Tìm câu hỏi theo loại câu hỏi
     */
    List<Question> findByQuestionType(Question.QuestionType questionType);

    /**
     * Custom query - Tìm câu hỏi theo lesson ID và lesson type
     */
    @Query("SELECT q FROM Question q WHERE " +
            "(q.lessonType = 'LISTENING' AND q.listeningLesson.id = :lessonId) OR " +
            "(q.lessonType = 'READING' AND q.readingLesson.id = :lessonId)")
    List<Question> findByLessonIdAndType(@Param("lessonId") Long lessonId);

    /**
     * Đếm số câu hỏi theo bài học
     */
    @Query("SELECT COUNT(q) FROM Question q WHERE q.listeningLesson.id = :lessonId")
    Long countByListeningLessonId(@Param("lessonId") Long lessonId);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.readingLesson.id = :lessonId")
    Long countByReadingLessonId(@Param("lessonId") Long lessonId);
}