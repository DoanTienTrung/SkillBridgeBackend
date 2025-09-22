package com.skillbridge.skillbridgebackend.controller;

import com.skillbridge.skillbridgebackend.dto.ForgotPasswordDto;
import com.skillbridge.skillbridgebackend.dto.ResetPasswordDto;
import com.skillbridge.skillbridgebackend.Service.UserService;
import com.skillbridge.skillbridgebackend.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
@Validated
@Slf4j
@Tag(name = "Password Reset", description = "Password reset functionality")
public class PasswordResetController {

    @Autowired
    private UserService userService;

    /**
     * Yêu cầu reset mật khẩu (bước 1)
     */
    @PostMapping("/forgot-password")
    @Operation(
            summary = "Request password reset",
            description = "Send password reset email to user"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Email sent successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            )
    })
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Parameter(description = "Email address", required = true)
            @Valid @RequestBody ForgotPasswordDto request) {

        try {
            log.info("Processing forgot password request for email: {}", request.getEmail());

            userService.processForgotPassword(request);

            // Luôn trả về success message (không tiết lộ email có tồn tại hay không)
            ApiResponse<String> response = ApiResponse.success(
                    "Nếu email tồn tại trong hệ thống, chúng tôi đã gửi link đặt lại mật khẩu đến hộp thư của bạn.",
                    "Email sent"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in forgot password request", e);

            ApiResponse<String> response = ApiResponse.error(
                    "Có lỗi xảy ra. Vui lòng thử lại sau.",
                    e.getMessage()
            );

            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Validate reset token (bước 2)
     */
    @GetMapping("/validate-reset-token")
    @Operation(
            summary = "Validate password reset token",
            description = "Check if reset token is valid and not expired"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateResetToken(
            @Parameter(description = "Reset token", required = true)
            @RequestParam String token) {

        try {
            log.info("Validating reset token: {}", token.substring(0, Math.min(10, token.length())) + "...");

            boolean isValid = userService.validateResetToken(token);

            Map<String, Object> result = new HashMap<>();
            result.put("valid", isValid);

            if (isValid) {
                ApiResponse<Map<String, Object>> response = ApiResponse.success(
                        "Token hợp lệ",
                        result
                );
                return ResponseEntity.ok(response);
            } else {
                // SỬA LỖI: Sử dụng constructor thay vì method error() với Map
                ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                        false,
                        "Token không hợp lệ hoặc đã hết hạn",
                        result
                );
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            log.error("Error validating reset token", e);

            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);

            // SỬA LỖI: Sử dụng constructor thay vì method error() với Map
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    false,
                    "Có lỗi xảy ra khi kiểm tra token",
                    result
            );

            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Reset mật khẩu (bước 3)
     */
    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset password with token",
            description = "Reset user password using valid reset token"
    )
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Parameter(description = "Password reset data", required = true)
            @Valid @RequestBody ResetPasswordDto request) {

        try {
            log.info("Processing password reset with token: {}",
                    request.getToken().substring(0, Math.min(10, request.getToken().length())) + "...");

            userService.resetPassword(request);

            ApiResponse<String> response = ApiResponse.success(
                    "Mật khẩu đã được cập nhật thành công. Vui lòng đăng nhập với mật khẩu mới.",
                    "Password reset successful"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error resetting password", e);

            ApiResponse<String> response = ApiResponse.error(
                    e.getMessage(),
                    "Password reset failed"
            );

            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}