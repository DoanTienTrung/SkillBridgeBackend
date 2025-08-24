package com.skillbridge.skillbridge_backend.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyRegistrationDto {
    private LocalDate date;
    private String dateLabel; // "Mon", "Tue", etc.
    private Integer newStudents;
    private Integer newTeachers;
    private Integer totalNewUsers;
}
