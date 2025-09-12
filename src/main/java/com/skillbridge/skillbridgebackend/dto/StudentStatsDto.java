package com.skillbridge.skillbridgebackend.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class StudentStatsDto {
    private Integer completedLessons;
    private Integer totalLessons;
    private Double averageScore;
    private Integer totalTimeStudied; // in seconds
    private Integer vocabularyCount;
    private List<Map<String, Object>> weeklyProgress;
}
