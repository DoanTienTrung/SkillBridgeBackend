package com.skillbridge.skillbridge_backend.controller;

import com.skillbridge.skillbridge_backend.Service.AnalyticsService;
import com.skillbridge.skillbridge_backend.dto.analytics.*;
import com.skillbridge.skillbridge_backend.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analytics")
@CrossOrigin(origins = "*")
@Tag(name = "Analytics", description = "Analytics and reporting endpoints for teachers and admins")
@PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
@Slf4j
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * Lấy analytics tổng quan hệ thống
     */
    @GetMapping("/system")
    @Operation(
        summary = "Get system analytics", 
        description = "Get overall system statistics including users, lessons, and activity data"
    )
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<SystemAnalyticsDto>> getSystemAnalytics() {
        try {
            log.info("Getting system analytics");
            SystemAnalyticsDto analytics = analyticsService.getSystemAnalytics();
            return ResponseEntity.ok(ApiResponse.success("Lấy thống kê hệ thống thành công", analytics));
        } catch (Exception e) {
            log.error("Error getting system analytics", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Không thể lấy thống kê hệ thống", e.getMessage()));
        }
    }

    /**
     * Lấy hoạt động 7 ngày qua
     */
    @GetMapping("/weekly-activity")
    @Operation(
        summary = "Get weekly activity", 
        description = "Get daily activity data for the past 7 days"
    )
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<List<DailyActivityDto>>> getWeeklyActivity() {
        try {
            log.info("Getting weekly activity");
            List<DailyActivityDto> activity = analyticsService.getWeeklyActivity();
            return ResponseEntity.ok(ApiResponse.success("Lấy hoạt động tuần thành công", activity));
        } catch (Exception e) {
            log.error("Error getting weekly activity", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Không thể lấy hoạt động tuần", e.getMessage()));
        }
    }

    /**
     * Lấy analytics tất cả bài học
     */
    @GetMapping("/lessons")
    @Operation(
        summary = "Get all lessons analytics", 
        description = "Get analytics data for all published lessons"
    )
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<List<LessonAnalyticsDto>>> getAllLessonsAnalytics() {
        try {
            log.info("Getting analytics for all lessons");
            List<LessonAnalyticsDto> analytics = analyticsService.getAllLessonsAnalytics();
            return ResponseEntity.ok(ApiResponse.success("Lấy thống kê bài học thành công", analytics));
        } catch (Exception e) {
            log.error("Error getting lessons analytics", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Không thể lấy thống kê bài học", e.getMessage()));
        }
    }

    /**
     * Lấy analytics một bài học cụ thể
     */
    @GetMapping("/lessons/{lessonId}")
    @Operation(
        summary = "Get lesson analytics", 
        description = "Get detailed analytics for a specific lesson"
    )
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<LessonAnalyticsDto>> getLessonAnalytics(
            @Parameter(description = "Lesson ID", required = true)
            @PathVariable Long lessonId,
            @Parameter(description = "Lesson type: LISTENING or READING", required = true)
            @RequestParam String lessonType) {
        try {
            log.info("Getting analytics for lesson: {} (type: {})", lessonId, lessonType);
            LessonAnalyticsDto analytics = analyticsService.getLessonAnalytics(lessonId, lessonType);
            return ResponseEntity.ok(ApiResponse.success("Lấy thống kê bài học thành công", analytics));
        } catch (Exception e) {
            log.error("Error getting lesson analytics for lesson: {} (type: {})", lessonId, lessonType, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Không thể lấy thống kê bài học", e.getMessage()));
        }
    }

    /**
     * Lấy báo cáo chi tiết của một học viên
     */
    @GetMapping("/students/{studentId}/report")
    @Operation(
        summary = "Get student progress report", 
        description = "Get detailed progress report for a specific student"
    )
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<StudentProgressReportDto>> getStudentReport(
            @Parameter(description = "Student ID", required = true)
            @PathVariable Long studentId) {
        try {
            log.info("Getting progress report for student: {}", studentId);
            StudentProgressReportDto report = analyticsService.getStudentReport(studentId);
            return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo học viên thành công", report));
        } catch (Exception e) {
            log.error("Error getting student report for student: {}", studentId, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Không thể lấy báo cáo học viên", e.getMessage()));
        }
    }

    /**
     * Lấy báo cáo tất cả học viên
     */
    @GetMapping("/students/reports")
    @Operation(
        summary = "Get all students reports", 
        description = "Get progress reports for all students"
    )
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<List<StudentProgressReportDto>>> getAllStudentsReports() {
        try {
            log.info("Getting progress reports for all students");
            List<StudentProgressReportDto> reports = analyticsService.getAllStudentsReports();
            return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo tất cả học viên thành công", reports));
        } catch (Exception e) {
            log.error("Error getting all students reports", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Không thể lấy báo cáo học viên", e.getMessage()));
        }
    }

    /**
     * Export Excel - Placeholder for now
     */
    @GetMapping("/export/students")
    @Operation(
        summary = "Export students data to Excel", 
        description = "Export all students data to Excel file"
    )
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<String>> exportStudentsExcel() {
        try {
            log.info("Exporting students data to Excel");
            // TODO: Implement Excel export using Apache POI
            return ResponseEntity.ok(ApiResponse.success("Tính năng xuất Excel sẽ được phát triển trong tương lai"));
        } catch (Exception e) {
            log.error("Error exporting students to Excel", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Không thể xuất Excel", e.getMessage()));
        }
    }

    /**
     * Export PDF - Placeholder for now
     */
    @GetMapping("/export/student/{studentId}/pdf")
    @Operation(
        summary = "Export student report to PDF", 
        description = "Export individual student progress report to PDF"
    )
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<String>> exportStudentPdf(
            @Parameter(description = "Student ID", required = true)
            @PathVariable Long studentId) {
        try {
            log.info("Exporting student {} report to PDF", studentId);
            // TODO: Implement PDF export using iText
            return ResponseEntity.ok(ApiResponse.success("Tính năng xuất PDF sẽ được phát triển trong tương lai"));
        } catch (Exception e) {
            log.error("Error exporting student {} report to PDF", studentId, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Không thể xuất PDF", e.getMessage()));
        }
    }
}
