package com.skillbridge.skillbridgebackend.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class StudentProgressDto {
    private StudentStatsDto overview;
    private Map<String, Object> charts;
    private List<RecentLessonDto> recentActivities;
}
