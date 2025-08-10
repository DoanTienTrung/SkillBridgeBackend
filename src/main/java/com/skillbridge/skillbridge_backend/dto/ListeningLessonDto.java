package com.skillbridge.skillbridge_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.skillbridge.skillbridge_backend.entity.ListeningLesson;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListeningLessonDto {

    private Long id;
    private String title;
    private String description;
    private String level;
    private String status;
    private String audioUrl;
    private String transcript;
    private Integer durationSeconds;

    // Category information
    private Long categoryId;
    private String categoryName;

    // Creator information
    private Long createdById;
    private String createdByName;

    // Additional fields
    private Integer questionCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Constructor from Entity
    public ListeningLessonDto(ListeningLesson lesson) {
        this.id = lesson.getId();
        this.title = lesson.getTitle();
        this.description = lesson.getDescription();
        this.level = lesson.getLevel().name();
        this.status = lesson.getStatus().name();
        this.audioUrl = lesson.getAudioUrl();
        this.transcript = lesson.getTranscript();
        this.durationSeconds = lesson.getDurationSeconds();
        this.createdAt = lesson.getCreatedAt();
        this.updatedAt = lesson.getUpdatedAt();

        // Category info
        if (lesson.getCategory() != null) {
            this.categoryId = lesson.getCategory().getId();
            this.categoryName = lesson.getCategory().getName();
        }

        // Creator info
        if (lesson.getCreatedBy() != null) {
            this.createdById = lesson.getCreatedBy().getId();
            this.createdByName = lesson.getCreatedBy().getFullName();
        }

        // Question count
        if (lesson.getQuestions() != null) {
            this.questionCount = lesson.getQuestions().size();
        }
    }

    // Static factory method
    public static ListeningLessonDto fromEntity(ListeningLesson lesson) {
        return new ListeningLessonDto(lesson);
    }
}