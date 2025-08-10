package com.skillbridge.skillbridge_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listening_lesson_id")
    private ListeningLesson listeningLesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reading_lesson_id")
    private ReadingLesson readingLesson;

    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_type", nullable = false)
    private LessonType lessonType;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    @NotNull(message = "Loại câu hỏi không được để trống")
    private QuestionType questionType;

    @Column(name = "option_a")
    private String optionA;

    @Column(name = "option_b")
    private String optionB;

    @Column(name = "option_c")
    private String optionC;

    @Column(name = "option_d")
    private String optionD;

    @Column(name = "correct_answer", nullable = false)
    @NotBlank(message = "Đáp án đúng không được để trống")
    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(nullable = false)
    private Integer points = 1;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Relationships
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserAnswer> userAnswers = new ArrayList<>();

    public enum LessonType {
        LISTENING("Bài nghe"),
        READING("Bài đọc");

        private final String displayName;

        LessonType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum QuestionType {
        MULTIPLE_CHOICE("Trắc nghiệm"),
        TRUE_FALSE("Đúng/Sai");

        private final String displayName;

        QuestionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}