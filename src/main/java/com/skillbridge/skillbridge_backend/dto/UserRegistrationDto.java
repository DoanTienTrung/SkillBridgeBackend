package com.skillbridge.skillbridge_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Schema(description = "User registration request data")
public class UserRegistrationDto {

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    @Schema(description = "User email address", example = "student@example.com", required = true)
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu phải có từ 6-100 ký tự")
    @Schema(description = "User password", example = "password123", required = true, minLength = 6)
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ tên phải có từ 2-100 ký tự")
    @Schema(description = "User full name", example = "Nguyễn Văn An", required = true)
    private String fullName;

    // Optional fields
    @Schema(description = "School/University name", example = "Đại học Bách Khoa Hà Nội")
    private String school;
    
    @Schema(description = "Major/Field of study", example = "Công nghệ thông tin")
    private String major;
    
    @Schema(description = "Academic year", example = "2023")
    private String academicYear;
    
    @Schema(description = "User role", example = "STUDENT", allowableValues = {"STUDENT", "TEACHER", "ADMIN"})
    private String role;
}