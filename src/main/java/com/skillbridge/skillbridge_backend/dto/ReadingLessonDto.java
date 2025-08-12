// Tạo file dto/ReadingLessonDto.java
package com.skillbridge.skillbridge_backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReadingLessonDto {
    private Long id;
    private String title;
    private String description;
    private String level;
    private String categoryName;
    private Long categoryId;
    private String content;
    private Integer wordCount;
    private String status;
    private String instructions;
    private String tags;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Related data
    private List<QuestionDto> questions;
    private List<LessonVocabularyDto> vocabularies;
}