// Tạo file dto/ListeningLessonUpdateDto.java
package com.skillbridge.skillbridge_backend.dto;

import com.skillbridge.skillbridge_backend.entity.ListeningLesson;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ListeningLessonUpdateDto {

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String description;

    @NotNull(message = "Cấp độ không được để trống")
    private ListeningLesson.Level level;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    private String transcript;

    @Positive(message = "Thời lượng phải là số dương")
    private Integer durationSeconds;

    // audioUrl không cho phép cập nhật vì cần upload lại file
}