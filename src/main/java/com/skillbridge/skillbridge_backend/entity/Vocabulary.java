package com.skillbridge.skillbridge_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vocabulary")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Từ vựng không được để trống")
    private String word;

    private String phonetic;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Nghĩa của từ không được để trống")
    private String meaning;

    @Column(name = "example_sentence", columnDefinition = "TEXT")
    private String exampleSentence;

    @Enumerated(EnumType.STRING)
    private ListeningLesson.Level level;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Relationships
    @OneToMany(mappedBy = "vocabulary", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserVocabulary> userVocabularyList = new ArrayList<>();
}