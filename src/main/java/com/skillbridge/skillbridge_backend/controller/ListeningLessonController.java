package com.skillbridge.skillbridge_backend.controller;

import com.skillbridge.skillbridge_backend.Service.ListeningLessonService;
import com.skillbridge.skillbridge_backend.dto.ListeningLessonCreateDto;
import com.skillbridge.skillbridge_backend.dto.ListeningLessonDto;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/listening-lessons")
@CrossOrigin(origins = "*")
@Tag(name = "Listening Lessons", description = "Listening lesson management endpoints")
public class ListeningLessonController {

    private final ListeningLessonService lessonService;
    private final JwtHelper jwtHelper;

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

        List<ListeningLesson> lessons;

        if (level != null && categoryId != null) {
            lessons = lessonService.getLessonsByLevelAndCategory(level, categoryId);
        } else {
            lessons = lessonService.getPublishedLessons();
        }

        List<ListeningLessonDto> lessonDtos = lessons.stream()
                .map(ListeningLessonMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bài học thành công", lessonDtos));
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

    @PutMapping("/{id}/publish")
    @Operation(summary = "Publish lesson", description = "Publish a draft lesson (Teacher/Admin only)")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ListeningLessonDto>> publishLesson(
            @Parameter(description = "Lesson ID", required = true)
            @PathVariable Long id) {
        ListeningLesson lesson = lessonService.publishLesson(id);
        ListeningLessonDto lessonDto = ListeningLessonMapper.toDto(lesson);

        return ResponseEntity.ok(ApiResponse.success("Xuất bản bài học thành công", lessonDto));
    }
}