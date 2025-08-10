package com.skillbridge.skillbridge_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.skillbridge.skillbridge_backend.entity.ListeningLesson;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListeningLessonCreateDto {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(min = 5, max = 255, message = "Tiêu đề phải có từ 5-255 ký tự")
    private String title;

    @Size(max = 1000, message = "Mô tả không được quá 1000 ký tự")
    private String description;

    @NotNull(message = "Cấp độ không được để trống")
    private ListeningLesson.Level level;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    @NotBlank(message = "URL âm thanh không được để trống")
    private String audioUrl;

    @Size(max = 5000, message = "Transcript không được quá 5000 ký tự")
    private String transcript;

    @Min(value = 1, message = "Thời lượng phải lớn hơn 0 giây")
    private Integer durationSeconds;
}