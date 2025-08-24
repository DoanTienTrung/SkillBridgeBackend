package com.skillbridge.skillbridge_backend.dto.analytics;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyActivityDto {
    private LocalDate date;
    private String dayName;
    private Integer activeUsers;
    private Integer completedLessons;
    private Integer newRegistrations;
    private Double averageScore;
}
