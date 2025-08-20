package com.skillbridge.skillbridge_backend.controller;

import com.skillbridge.skillbridge_backend.dto.UserDto;
import com.skillbridge.skillbridge_backend.dto.UserRegistrationDto;
import com.skillbridge.skillbridge_backend.dto.*;
import com.skillbridge.skillbridge_backend.entity.User;
import com.skillbridge.skillbridge_backend.Service.UserService;
import com.skillbridge.skillbridge_backend.response.ApiResponse;
import com.skillbridge.skillbridge_backend.security.JwtHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
@Tag(name = "User Management", description = "User profile and management endpoints")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtHelper jwtHelper;

    /**
     * Lấy thông tin profile của user hiện tại
     */
    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Get current user's profile information")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<UserDto>> getProfile(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userService.findByEmail(email);
            UserDto userDto = new UserDto(user);

            return ResponseEntity.ok(ApiResponse.success(userDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Không thể lấy thông tin profile"));
        }
    }

    /**
     * Cập nhật profile
     */
    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Update current user's profile information")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            Authentication authentication,
            @Parameter(description = "Updated profile data", required = true)
            @Valid @RequestBody UserRegistrationDto updateDto) {

        try {
            String email = authentication.getName();
            User currentUser = userService.findByEmail(email);

            User updatedUser = userService.updateUser(currentUser.getId(), updateDto);
            UserDto userDto = new UserDto(updatedUser);

            return ResponseEntity.ok(ApiResponse.success("Cập nhật profile thành công", userDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Không thể cập nhật profile", e.getMessage()));
        }
    }

    /**
     * Lấy danh sách tất cả học viên (chỉ cho teacher/admin)
     */
    @GetMapping("/students")
    @Operation(summary = "Get all students", description = "Get list of all students (Teacher/Admin only)")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllStudents() {
        try {
            List<User> students = userService.getAllStudents();
            List<UserDto> studentDtos = students.stream()
                    .map(UserDto::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách học viên thành công", studentDtos));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Không thể lấy danh sách học viên"));
        }
    }

    /**
     * Lấy thông tin user theo ID (chỉ cho admin)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get user information by ID (Admin only)")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id) {
        try {
            User user = userService.findById(id);
            UserDto userDto = new UserDto(user);

            return ResponseEntity.ok(ApiResponse.success(userDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Không tìm thấy người dùng"));
        }
    }

    /**
     * Kích hoạt/vô hiệu hóa user (chỉ cho admin)
     */
    @PutMapping("/{id}/toggle-active")
    @Operation(summary = "Toggle user active status", description = "Activate/Deactivate user account (Admin only)")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> toggleUserActive(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id) {
        try {
            User user = userService.toggleUserActive(id);
            UserDto userDto = new UserDto(user);

            return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", userDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Không thể cập nhật trạng thái user"));
        }
    }

    // ===== STUDENT-SPECIFIC ENDPOINTS =====

    /**
     * Get student statistics for dashboard (Student only)
     */
    @GetMapping("/student/stats")
    @Operation(summary = "Get student statistics", description = "Get student learning statistics for dashboard")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentStatsDto>> getStudentStats() {
        try {
            User currentUser = jwtHelper.getCurrentUser();
            StudentStatsDto stats = userService.getStudentStats(currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success("Lấy thống kê thành công", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy thống kê", e.getMessage()));
        }
    }

    /**
     * Get recent lessons for student (Student only)
     */
    @GetMapping("/student/recent-lessons")
    @Operation(summary = "Get recent lessons", description = "Get recently accessed lessons")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<RecentLessonDto>>> getRecentLessons(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            User currentUser = jwtHelper.getCurrentUser();
            List<RecentLessonDto> lessons = userService.getRecentLessons(currentUser.getId(), limit);
            return ResponseEntity.ok(ApiResponse.success("Lấy bài học gần đây thành công", lessons));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy bài học gần đây", e.getMessage()));
        }
    }

    /**
     * Get published lessons for students (Student only)
     */
    @GetMapping("/student/lessons")
    @Operation(summary = "Get published lessons", description = "Get all published lessons with filtering")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<LessonDto>>> getPublishedLessons(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search) {
        try {
            List<LessonDto> lessons = userService.getPublishedLessons(type, level, categoryId, search);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bài học thành công", lessons));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy danh sách bài học", e.getMessage()));
        }
    }

    /**
     * Get lesson by ID for student view (Student only)
     */
    @GetMapping("/student/lessons/{type}/{id}")
    @Operation(summary = "Get lesson details", description = "Get lesson details for student")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<LessonDto>> getLessonForStudent(
            @PathVariable String type,
            @PathVariable Long id) {
        try {
            LessonDto lesson = userService.getLessonForStudent(id, type);
            return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết bài học thành công", lesson));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy chi tiết bài học", e.getMessage()));
        }
    }

    /**
     * Submit student answers (Student only)
     */
    @PostMapping("/student/submit-answers")
    @Operation(summary = "Submit lesson answers", description = "Submit student answers and get score")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<SubmissionResultDto>> submitAnswers(
            @RequestBody SubmissionDto submissionDto) {
        try {
            User currentUser = jwtHelper.getCurrentUser();
            SubmissionResultDto result = userService.submitAnswers(currentUser.getId(), submissionDto);
            return ResponseEntity.ok(ApiResponse.success("Nộp bài thành công", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi nộp bài", e.getMessage()));
        }
    }

    /**
     * Get student progress data for analytics (Student only)
     */
    @GetMapping("/student/progress")
    @Operation(summary = "Get progress data", description = "Get student progress analytics")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentProgressDto>> getProgressData(
            @RequestParam(defaultValue = "week") String timeRange) {
        try {
            User currentUser = jwtHelper.getCurrentUser();
            StudentProgressDto progress = userService.getProgressData(currentUser.getId(), timeRange);
            return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu tiến độ thành công", progress));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy dữ liệu tiến độ", e.getMessage()));
        }
    }
}