package com.skillbridge.skillbridge_backend.dto.analytics;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonAnalyticsDto {
    private Long lessonId;
    private String lessonTitle;
    private String lessonType; // "LISTENING" hoặc "READING"
    private String level;
    private String categoryName;
    private Integer totalViews;
    private Integer completedCount;
    private Double completionRate;
    private Double averageScore;
    private Integer averageTimeSpent;
    private Integer totalQuestions;
    private List<QuestionAnalyticsDto> questionStats;
}
