package com.skillbridge.skillbridge_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reading_lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadingLesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Cấp độ không được để trống")
    private ListeningLesson.Level level; // Sử dụng chung enum Level

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    @Column(name = "word_count")
    private Integer wordCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListeningLesson.Status status = ListeningLesson.Status.DRAFT; // Sử dụng chung enum Status

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "readingLesson", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Question> questions = new ArrayList<>();

    // Note: UserLessonProgress relationship is handled through lessonId and lessonType
    // No direct @OneToMany mapping needed since UserLessonProgress uses polymorphic approach
}