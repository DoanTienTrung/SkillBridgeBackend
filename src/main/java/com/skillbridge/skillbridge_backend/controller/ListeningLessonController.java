package com.skillbridge.skillbridge_backend.controller;

import com.skillbridge.skillbridge_backend.Service.ListeningLessonService;
import com.skillbridge.skillbridge_backend.dto.LessonPreviewDto;
import com.skillbridge.skillbridge_backend.dto.ListeningLessonCreateDto;
import com.skillbridge.skillbridge_backend.dto.ListeningLessonDto;
import com.skillbridge.skillbridge_backend.dto.ListeningLessonUpdateDto;
import com.skillbridge.skillbridge_backend.entity.ListeningLesson;
import com.skillbridge.skillbridge_backend.entity.User;
import com.skillbridge.skillbridge_backend.mapper.ListeningLessonMapper;
import com.skillbridge.skillbridge_backend.response.ApiResponse;
import com.skillbridge.skillbridge_backend.security.JwtHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/listening-lessons")
@CrossOrigin(origins = "*")
@Tag(name = "Listening Lessons", description = "Listening lesson management endpoints")
public class ListeningLessonController {

    private final ListeningLessonService lessonService;
    private final JwtHelper jwtHelper;

    private static final Logger logger = LoggerFactory.getLogger(ListeningLessonController.class);

    public ListeningLessonController(ListeningLessonService lessonService, JwtHelper jwtHelper) {
        this.lessonService = lessonService;
        this.jwtHelper = jwtHelper;
    }

    @GetMapping
    @Operation(
        summary = "Get all listening lessons", 
        description = "Get list of listening lessons with optional filtering by level and category"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Lessons retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<List<ListeningLessonDto>>> getAllLessons(
            @Parameter(description = "Filter by difficulty level")
            @RequestParam(required = false) ListeningLesson.Level level,
            @Parameter(description = "Filter by category ID")
            @RequestParam(required = false) Long categoryId) {

        // Log start of method with parameters
        logger.info("=== START getAllLessons ===");
        logger.info("Request parameters - level: {}, categoryId: {}", level, categoryId);

        try {
            List<ListeningLesson> lessons;

            // Log filtering logic decision
            if (level != null && categoryId != null) {
                logger.info("Applying both level and category filters");
                logger.debug("Filtering by level: {} and categoryId: {}", level, categoryId);

                lessons = lessonService.getLessonsByLevelAndCategory(level, categoryId);

                logger.info("Found {} lessons with level: {} and categoryId: {}",
                        lessons != null ? lessons.size() : 0, level, categoryId);

            } else if (level != null) {
                logger.info("Applying level filter only");
                logger.debug("Filtering by level: {}", level);

                // Note: This logic path doesn't exist in original code, but might be useful
                lessons = lessonService.getPublishedLessons(); // You might want to add getLessonsByLevel method
                logger.warn("Level-only filtering not implemented, falling back to published lessons");

            } else if (categoryId != null) {
                logger.info("Applying category filter only");
                logger.debug("Filtering by categoryId: {}", categoryId);

                // Note: This logic path doesn't exist in original code, but might be useful
                lessons = lessonService.getPublishedLessons(); // You might want to add getLessonsByCategory method
                logger.warn("Category-only filtering not implemented, falling back to published lessons");

            } else {
                logger.info("No filters applied, getting all published lessons");
                lessons = lessonService.getPublishedLessons();

                logger.info("Found {} published lessons",
                        lessons != null ? lessons.size() : 0);
            }

            // Log lessons retrieval result
            if (lessons == null) {
                logger.warn("Service returned null lessons list");
                lessons = new ArrayList<>();
            }

            if (lessons.isEmpty()) {
                logger.info("No lessons found matching criteria");
            } else {
                logger.debug("Lessons details: {}",
                        lessons.stream()
                                .map(lesson -> String.format("ID:%d, Title:'%s', Level:%s, Status:%s",
                                        lesson.getId(), lesson.getTitle(), lesson.getLevel(), lesson.getStatus()))
                                .collect(Collectors.joining(", ")));
            }

            // Log DTO conversion
            logger.info("Converting {} lessons to DTOs", lessons.size());
            List<ListeningLessonDto> lessonDtos = lessons.stream()
                    .map(lesson -> {
                        try {
                            ListeningLessonDto dto = ListeningLessonMapper.toDto(lesson);
                            logger.debug("Converted lesson ID: {} to DTO successfully", lesson.getId());
                            return dto;
                        } catch (Exception e) {
                            logger.error("Failed to convert lesson ID: {} to DTO: {}",
                                    lesson.getId(), e.getMessage());
                            throw e;
                        }
                    })
                    .collect(Collectors.toList());

            logger.info("Successfully converted {} lessons to DTOs", lessonDtos.size());

            // Log successful response
            ApiResponse<List<ListeningLessonDto>> response =
                    ApiResponse.success("Lấy danh sách bài học thành công", lessonDtos);

            logger.info("=== END getAllLessons SUCCESS - Returning {} lessons ===", lessonDtos.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Log any exceptions
            logger.error("=== END getAllLessons ERROR ===");
            logger.error("Exception in getAllLessons with level: {}, categoryId: {}", level, categoryId, e);

            // You might want to return an error response instead of letting exception propagate
            throw e;
        }
    }


    @GetMapping("/{id}")
    @Operation(summary = "Get lesson by ID", description = "Get detailed information of a specific lesson")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lesson found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lesson not found")
    })
    public ResponseEntity<ApiResponse<ListeningLessonDto>> getLessonById(
            @Parameter(description = "Lesson ID", required = true)
            @PathVariable Long id) {
        ListeningLesson lesson = lessonService.findById(id);
        ListeningLessonDto lessonDto = ListeningLessonMapper.toDto(lesson);

        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin bài học thành công", lessonDto));
    }

    @PostMapping
    @Operation(summary = "Create new lesson", description = "Create a new listening lesson (Teacher/Admin only)")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ListeningLessonDto>> createLesson(
            @Parameter(description = "Lesson data", required = true)
            @Valid @RequestBody ListeningLessonCreateDto createDto) {

        User currentUser = jwtHelper.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        ListeningLesson lesson = lessonService.createLesson(createDto, currentUser.getId());
        ListeningLessonDto lessonDto = ListeningLessonMapper.toDto(lesson);

        return ResponseEntity.ok(ApiResponse.success("Tạo bài học thành công", lessonDto));
    }

    @GetMapping("/{id}/preview")
    @Operation(summary = "Preview lesson", description = "Get lesson preview data")
    public ResponseEntity<ApiResponse<LessonPreviewDto>> previewLesson(
            @PathVariable Long id) {

        LessonPreviewDto preview = lessonService.getPreviewData(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy preview thành công", preview));
    }

    @PutMapping("/{id}/publish")
    @Operation(summary = "Publish lesson", description = "Publish lesson after validation")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ListeningLessonDto>> publishLesson(
            @PathVariable Long id) {

        // Validate trước khi publish
        List<String> validationErrors = lessonService.validateLessonForPublish(id);
        if (!validationErrors.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Validation failed: " + String.join(", ", validationErrors)));
        }

        ListeningLesson lesson = lessonService.publishLesson(id);
        ListeningLessonDto lessonDto = ListeningLessonMapper.toDto(lesson);

        return ResponseEntity.ok(ApiResponse.success("Xuất bản bài học thành công", lessonDto));
    }

    // Thêm vào ListeningLessonController.java

    @GetMapping("/admin")
    @Operation(summary = "Get all lessons for admin/teacher", description = "Get all lessons including drafts")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ListeningLessonDto>>> getAllLessonsForAdmin() {
        User currentUser = jwtHelper.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        List<ListeningLesson> lessons = lessonService.getAllLessonsForAdmin(currentUser.getId());
        List<ListeningLessonDto> lessonDtos = lessons.stream()
                .map(ListeningLessonMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bài học thành công", lessonDtos));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update lesson", description = "Update lesson information")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ListeningLessonDto>> updateLesson(
            @PathVariable Long id,
            @Valid @RequestBody ListeningLessonUpdateDto updateDto) {

        User currentUser = jwtHelper.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        try {
            ListeningLesson updatedLesson = lessonService.updateLesson(id, updateDto, currentUser.getId());
            ListeningLessonDto lessonDto = ListeningLessonMapper.toDto(updatedLesson);

            return ResponseEntity.ok(ApiResponse.success("Cập nhật bài học thành công", lessonDto));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete lesson", description = "Delete a lesson")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteLesson(@PathVariable Long id) {
        User currentUser = jwtHelper.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        try {
            boolean deleted = lessonService.deleteLesson(id, currentUser.getId());
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success("Xóa bài học thành công", null));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Không thể xóa bài học"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update lesson status", description = "Change lesson status between DRAFT and PUBLISHED")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ListeningLessonDto>> updateLessonStatus(
            @PathVariable Long id,
            @RequestParam ListeningLesson.Status status) {

        User currentUser = jwtHelper.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        try {
            ListeningLesson updatedLesson = lessonService.updateLessonStatus(id, status, currentUser.getId());
            ListeningLessonDto lessonDto = ListeningLessonMapper.toDto(updatedLesson);

            return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", lessonDto));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Thêm vào cuối file ListeningLessonController.java

    @GetMapping("/{id}/validation")
    @Operation(summary = "Validate lesson for publish", description = "Check if lesson meets publish requirements")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<String>>> validateLesson(@PathVariable Long id) {
        List<String> validationErrors = lessonService.validateLessonForPublish(id);

        if (validationErrors.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("Bài học hợp lệ để xuất bản", validationErrors));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Bài học chưa đủ điều kiện xuất bản", String.valueOf(validationErrors)));
        }
    }
}