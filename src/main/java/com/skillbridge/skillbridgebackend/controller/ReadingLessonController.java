// Tạo file controller/ReadingLessonController.java
package com.skillbridge.skillbridgebackend.controller;

import com.skillbridge.skillbridgebackend.Service.ReadingLessonService;
import com.skillbridge.skillbridgebackend.dto.ReadingLessonCreateDto;
import com.skillbridge.skillbridgebackend.dto.ReadingLessonDto;
import com.skillbridge.skillbridgebackend.entity.ReadingLesson;
import com.skillbridge.skillbridgebackend.entity.ListeningLesson;
import com.skillbridge.skillbridgebackend.entity.User;
import com.skillbridge.skillbridgebackend.mapper.ReadingLessonMapper;
import com.skillbridge.skillbridgebackend.response.ApiResponse;
import com.skillbridge.skillbridgebackend.security.JwtHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reading-lessons")
@CrossOrigin(origins = "*")
@Tag(name = "Reading Lessons", description = "Reading lesson management endpoints")
public class ReadingLessonController {

    private final ReadingLessonService readingLessonService;
    private final JwtHelper jwtHelper;

    public ReadingLessonController(ReadingLessonService readingLessonService, JwtHelper jwtHelper) {
        this.readingLessonService = readingLessonService;
        this.jwtHelper = jwtHelper;
    }

    @PostMapping
    @Operation(summary = "Create new reading lesson", description = "Create a new reading lesson (Teacher/Admin only)")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReadingLessonDto>> createReadingLesson(
            @Valid @RequestBody ReadingLessonCreateDto createDto) {

        User currentUser = jwtHelper.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        ReadingLesson lesson = readingLessonService.createReadingLesson(createDto, currentUser.getId());
        ReadingLessonDto lessonDto = ReadingLessonMapper.toDto(lesson);

        return ResponseEntity.ok(ApiResponse.success("Tạo bài đọc thành công", lessonDto));
    }

    @GetMapping("/admin")
    @Operation(summary = "Get all reading lessons for admin/teacher")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ReadingLessonDto>>> getAllReadingLessonsForAdmin() {
        User currentUser = jwtHelper.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        List<ReadingLesson> lessons = readingLessonService.getAllReadingLessonsForAdmin(currentUser.getId());
        List<ReadingLessonDto> lessonDtos = lessons.stream()
                .map(ReadingLessonMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bài đọc thành công", lessonDtos));
    }

    @GetMapping
    @Operation(summary = "Get published reading lessons")
    public ResponseEntity<ApiResponse<List<ReadingLessonDto>>> getPublishedReadingLessons() {
        List<ReadingLesson> lessons = readingLessonService.getPublishedReadingLessons();
        List<ReadingLessonDto> lessonDtos = lessons.stream()
                .map(ReadingLessonMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bài đọc thành công", lessonDtos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reading lesson by ID")
    public ResponseEntity<ApiResponse<ReadingLessonDto>> getReadingLessonById(@PathVariable Long id) {
        ReadingLesson lesson = readingLessonService.findById(id);
        ReadingLessonDto lessonDto = ReadingLessonMapper.toDto(lesson);

        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin bài đọc thành công", lessonDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update reading lesson")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReadingLessonDto>> updateReadingLesson(
            @PathVariable Long id,
            @Valid @RequestBody ReadingLessonCreateDto updateDto) {

        User currentUser = jwtHelper.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        try {
            ReadingLesson updatedLesson = readingLessonService.updateReadingLesson(id, updateDto, currentUser.getId());
            ReadingLessonDto lessonDto = ReadingLessonMapper.toDto(updatedLesson);

            return ResponseEntity.ok(ApiResponse.success("Cập nhật bài đọc thành công", lessonDto));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete reading lesson")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteReadingLesson(@PathVariable Long id) {
        User currentUser = jwtHelper.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        try {
            boolean deleted = readingLessonService.deleteReadingLesson(id, currentUser.getId());
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success("Xóa bài đọc thành công", null));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Không thể xóa bài đọc"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update reading lesson status")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReadingLessonDto>> updateReadingLessonStatus(
            @PathVariable Long id,
            @RequestParam ListeningLesson.Status status) {

        User currentUser = jwtHelper.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        try {
            ReadingLesson updatedLesson = readingLessonService.updateReadingLessonStatus(id, status, currentUser.getId());
            ReadingLessonDto lessonDto = ReadingLessonMapper.toDto(updatedLesson);

            return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", lessonDto));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/upload-text")
    @Operation(summary = "Upload text file for reading lesson")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> uploadTextFile(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File không được để trống"));
        }

        // Kiểm tra file type
        String contentType = file.getContentType();
        if (!contentType.equals("text/plain")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Chỉ chấp nhận file .txt"));
        }

        try {
            byte[] fileContent = file.getBytes();
            String processedText = readingLessonService.processTextFile(fileContent);

            return ResponseEntity.ok(ApiResponse.success("Upload file thành công", processedText));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi xử lý file: " + e.getMessage()));
        }
    }
}