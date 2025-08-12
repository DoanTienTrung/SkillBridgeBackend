package com.skillbridge.skillbridge_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Removed unused imports


@Entity
@Table(name = "lesson_vocabulary")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonVocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private ListeningLesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id", nullable = false)
    private Vocabulary vocabulary;


    @Column(name = "start_position", nullable = false)
    private Integer startPosition;

    @Column(name = "end_position", nullable = false)
    private Integer endPosition;

    @Column(name = "highlight_color", length = 7)
    private String highlightColor = "#ffeb3b";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


}