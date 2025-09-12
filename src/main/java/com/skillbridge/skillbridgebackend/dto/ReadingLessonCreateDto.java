// Tạo file dto/ReadingLessonCreateDto.java
package com.skillbridge.skillbridgebackend.dto;

import com.skillbridge.skillbridgebackend.entity.ListeningLesson;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReadingLessonCreateDto {

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String description;

    @NotNull(message = "Cấp độ không được để trống")
    private ListeningLesson.Level level;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    private Integer wordCount; // Tự động tính từ content

    private String instructions = "Đọc đoạn văn sau và trả lời các câu hỏi bên dưới.";

    private String tags; // Keywords for SEO
}