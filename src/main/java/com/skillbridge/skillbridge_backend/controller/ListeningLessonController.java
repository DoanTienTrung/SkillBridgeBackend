package com.skillbridge.skillbridge_backend.controller;

import com.skillbridge.skillbridge_backend.Service.ListeningLessonService;
import com.skillbridge.skillbridge_backend.dto.ListeningLessonCreateDto;
import com.skillbridge.skillbridge_backend.dto.ListeningLessonDto;
import com.skillbridge.skillbridge_backend.entity.ListeningLesson;
import com.skillbridge.skillbridge_backend.entity.User;
import com.skillbridge.skillbridge_backend.mapper.ListeningLessonMapper;
import com.skillbridge.skillbridge_backend.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/listening-lessons")
@CrossOrigin(origins = "*")
public class ListeningLessonController {

    private final ListeningLessonService lessonService;

    public ListeningLessonController(ListeningLessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ListeningLessonDto>>> getAllLessons(
            @RequestParam(required = false) ListeningLesson.Level level,
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
    public ResponseEntity<ApiResponse<ListeningLessonDto>> getLessonById(@PathVariable Long id) {
        ListeningLesson lesson = lessonService.findById(id);
        ListeningLessonDto lessonDto = ListeningLessonMapper.toDto(lesson);

        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin bài học thành công", lessonDto));
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ListeningLessonDto>> createLesson(
            @Valid @RequestBody ListeningLessonCreateDto createDto,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        ListeningLesson lesson = lessonService.createLesson(createDto, currentUser.getId());
        ListeningLessonDto lessonDto = ListeningLessonMapper.toDto(lesson);

        return ResponseEntity.ok(ApiResponse.success("Tạo bài học thành công", lessonDto));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ListeningLessonDto>> publishLesson(@PathVariable Long id) {
        ListeningLesson lesson = lessonService.publishLesson(id);
        ListeningLessonDto lessonDto = ListeningLessonMapper.toDto(lesson);

        return ResponseEntity.ok(ApiResponse.success("Xuất bản bài học thành công", lessonDto));
    }
}