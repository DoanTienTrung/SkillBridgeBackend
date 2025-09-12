package com.skillbridge.skillbridgebackend.repository;

import com.skillbridge.skillbridgebackend.entity.LessonVocabulary;
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