package com.skillbridge.skillbridge_backend.dto.analytics;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemAnalyticsDto {
    private Integer totalUsers;
    private Integer totalStudents;
    private Integer totalTeachers;
    private Integer totalListeningLessons;
    private Integer totalReadingLessons;
    private Integer totalQuestions;
    private Integer totalVocabulary;
    private Integer activeUsersToday;
    private Integer newRegistrationsThisWeek;
    private Integer completedLessonsToday;
    private List<DailyActivityDto> weeklyActivity;
}
