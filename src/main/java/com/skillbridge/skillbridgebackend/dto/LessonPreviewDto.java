package com.skillbridge.skillbridgebackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonPreviewDto {
    private Long id;
    private String title;
    private String description;
    private String level;
    private String categoryName;
    private String audioUrl;
    private String transcript;
    private Integer durationSeconds;
    private String status;
    private List<QuestionDto> questions;
    private List<LessonVocabularyDto> vocabularies;
    private String createdBy;
    private String createdAt;
}