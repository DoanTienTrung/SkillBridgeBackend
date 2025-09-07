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
    
    // New fields for personal vocabulary
    @Column(name = "category")
    private String category;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private Difficulty difficulty;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "part_of_speech")
    private PartOfSpeech partOfSpeech;
    
    @Column(name = "synonyms", columnDefinition = "TEXT")
    private String synonyms;
    
    @Column(name = "antonyms", columnDefinition = "TEXT")
    private String antonyms;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Relationships
    @OneToMany(mappedBy = "vocabulary", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserVocabulary> userVocabularyList = new ArrayList<>();
    
    // Enums
    public enum Difficulty {
        EASY("Dễ"),
        MEDIUM("Trung bình"),
        HARD("Khó");
        
        private final String displayName;
        
        Difficulty(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum PartOfSpeech {
        NOUN("Danh từ"),
        VERB("Động từ"),
        ADJECTIVE("Tính từ"),
        ADVERB("Trạng từ"),
        PRONOUN("Đại từ"),
        PREPOSITION("Giới từ"),
        CONJUNCTION("Liên từ"),
        INTERJECTION("Thán từ"),
        ARTICLE("Mạo từ"),
        PHRASE("Cụm từ");
        
        private final String displayName;
        
        PartOfSpeech(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}