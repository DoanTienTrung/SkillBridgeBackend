package com.skillbridge.skillbridge_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_lesson_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "lesson_id", "lesson_type"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_type", nullable = false)
    private Question.LessonType lessonType;

    // Virtual relationship (không thể dùng @ManyToOne vì lesson có thể là ListeningLesson hoặc ReadingLesson)
    @Transient
    private Object lesson; // Sẽ được set trong service layer

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds = 0;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}