package com.skillbridge.skillbridgebackend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RecentLessonDto {
    private Long lessonId;
    private String lessonTitle;
    private String lessonType;
    private Double score;
    private Integer timeSpent;
    private LocalDateTime completedAt;
    private Boolean isCompleted;
}
