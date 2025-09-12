package com.skillbridge.skillbridgebackend.dto.analytics;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentProgressReportDto {
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String school;
    private String major;
    private String academicYear;
    private Integer totalLessonsCompleted;
    private Integer listeningLessonsCompleted;
    private Integer readingLessonsCompleted;
    private Double averageScore;
    private Integer totalTimeStudiedSeconds;
    private Integer vocabularyLearned;
    private LocalDateTime lastActivity;
    private LocalDateTime registrationDate;
    private List<LessonProgressDetailDto> lessonDetails;
}
