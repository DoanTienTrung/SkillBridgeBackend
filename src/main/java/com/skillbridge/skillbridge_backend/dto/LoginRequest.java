package com.skillbridge.skillbridge_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "User login request data")
public class LoginRequest {

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    @Schema(description = "User email", example = "student@example.com", required = true)
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Schema(description = "User password", example = "password123", required = true)
    private String password;
}