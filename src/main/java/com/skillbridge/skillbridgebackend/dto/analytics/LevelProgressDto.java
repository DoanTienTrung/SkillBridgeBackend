package com.skillbridge.skillbridgebackend.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LevelProgressDto {
    // A2 Level
    private Integer a2LessonsCompleted;
    private Integer a2TotalLessons;
    private Double a2CompletionRate;
    private BigDecimal a2AverageScore;
    
    // B1 Level
    private Integer b1LessonsCompleted;
    private Integer b1TotalLessons;
    private Double b1CompletionRate;
    private BigDecimal b1AverageScore;
    
    // B2 Level
    private Integer b2LessonsCompleted;
    private Integer b2TotalLessons;
    private Double b2CompletionRate;
    private BigDecimal b2AverageScore;
    
    // C1 Level
    private Integer c1LessonsCompleted;
    private Integer c1TotalLessons;
    private Double c1CompletionRate;
    private BigDecimal c1AverageScore;
    
    // Current recommended level
    private String recommendedLevel;
    private String nextLevelSuggestion;
}
