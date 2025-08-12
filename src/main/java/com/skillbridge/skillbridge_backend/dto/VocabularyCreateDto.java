package com.skillbridge.skillbridge_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyCreateDto {

    @NotBlank(message = "Từ vựng không được để trống")
    private String word;

    private String phonetic;

    @NotBlank(message = "Nghĩa của từ không được để trống")
    private String meaning;

    private String exampleSentence;

    @NotNull(message = "Vị trí bắt đầu không được để trống")
    @Min(value = 0, message = "Vị trí bắt đầu phải >= 0")
    private Integer startPosition;

    @NotNull(message = "Vị trí kết thúc không được để trống")
    @Min(value = 0, message = "Vị trí kết thúc phải >= 0")
    private Integer endPosition;

    private String highlightColor = "#ffeb3b";
}