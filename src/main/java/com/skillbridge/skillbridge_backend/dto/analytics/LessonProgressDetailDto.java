package com.skillbridge.skillbridge_backend.dto.analytics;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgressDetailDto {
    private Long lessonId;
    private String lessonTitle;
    private String lessonType; // "LISTENING" hoặc "READING"
    private String level;
    private String categoryName;
    private Boolean isCompleted;
    private BigDecimal score;
    private Integer timeSpentSeconds;
    private LocalDateTime completedAt;
    private Integer totalQuestions;
    private Integer correctAnswers;
}
