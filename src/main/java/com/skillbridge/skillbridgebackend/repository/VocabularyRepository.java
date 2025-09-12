package com.skillbridge.skillbridgebackend.repository;

import com.skillbridge.skillbridgebackend.entity.Vocabulary;
import com.skillbridge.skillbridgebackend.entity.ListeningLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {

    /**
     * Tìm từ vựng theo word
     */
    Optional<Vocabulary> findByWord(String word);

    /**
     * Tìm từ vựng theo level
     */
    List<Vocabulary> findByLevel(ListeningLesson.Level level);

    /**
     * Search từ vựng
     */
    List<Vocabulary> findByWordContainingIgnoreCase(String word);

    /**
     * Tìm từ vựng theo nghĩa
     */
    List<Vocabulary> findByMeaningContainingIgnoreCase(String meaning);

    /**
     * Check từ vựng đã tồn tại
     */
    boolean existsByWord(String word);
}