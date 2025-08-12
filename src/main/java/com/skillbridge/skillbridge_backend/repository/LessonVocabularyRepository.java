package com.skillbridge.skillbridge_backend.repository;

import com.skillbridge.skillbridge_backend.entity.LessonVocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonVocabularyRepository extends JpaRepository<LessonVocabulary, Long> {

    List<LessonVocabulary> findByLessonIdOrderByStartPosition(Long lessonId);

    @Query("SELECT lv FROM LessonVocabulary lv WHERE lv.lesson.id = :lessonId " +
            "AND lv.startPosition <= :endPos AND lv.endPosition >= :startPos")
    List<LessonVocabulary> findOverlappingVocabularies(
            @Param("lessonId") Long lessonId,
            @Param("startPos") Integer startPos,
            @Param("endPos") Integer endPos
    );

    Optional<LessonVocabulary> findByLessonIdAndVocabularyId(Long lessonId, Long vocabularyId);
}