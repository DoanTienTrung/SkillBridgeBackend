package com.skillbridge.skillbridgebackend.controller;

import com.skillbridge.skillbridgebackend.dto.GoogleAuthDto;
import com.skillbridge.skillbridgebackend.dto.GoogleUserProfileDto;
import com.skillbridge.skillbridgebackend.dto.LoginResponse;
import com.skillbridge.skillbridgebackend.dto.UserDto;
import com.skillbridge.skillbridgebackend.entity.User;
import com.skillbridge.skillbridgebackend.response.ApiResponse;
import com.skillbridge.skillbridgebackend.security.JwtUtil;
import com.skillbridge.skillbridgebackend.Service.GoogleOAuthService;
import com.skillbridge.skillbridgebackend.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/google")
@CrossOrigin(origins = "*")
@Slf4j
@Tag(name = "Google Authentication", description = "Google OAuth2 authentication endpoints")
public class GoogleAuthController {

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Lấy Google OAuth authorization URL
     */
    @GetMapping("/url")
    @Operation(summary = "Get Google OAuth URL", description = "Get authorization URL for Google OAuth login")
    public ResponseEntity<ApiResponse<Map<String, String>>> getGoogleAuthUrl() {
        try {
            log.info("Generating Google OAuth authorization URL");

            String authUrl = googleOAuthService.getAuthorizationUrl();

            Map<String, String> response = new HashMap<>();
            response.put("authUrl", authUrl);

            return ResponseEntity.ok(ApiResponse.success("Google auth URL generated", response));

        } catch (Exception e) {
            log.error("Error generating Google auth URL", e);
            return new ResponseEntity<>(
                    ApiResponse.error("Failed to generate Google auth URL", e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Xử lý Google OAuth callback (redirect từ Google)
     */
    @GetMapping("/callback")
    @Operation(summary = "Google OAuth callback", description = "Handle Google OAuth callback with authorization code")
    public void handleGoogleCallback(
            @Parameter(description = "Authorization code from Google")
            @RequestParam String code,
            @Parameter(description = "State parameter for CSRF protection")
            @RequestParam(required = false) String state,
            HttpServletResponse response) throws IOException {

        try {
            log.info("Processing Google OAuth callback");

            // Process authorization code
            GoogleUserProfileDto googleProfile = googleOAuthService.processAuthorizationCode(code);

            // Create or update user
            User user = userService.createOrUpdateGoogleUser(googleProfile);

            // Generate JWT token
            String jwtToken = jwtUtil.generateToken(user);

            // Redirect to frontend with token
            String redirectUrl = frontendUrl + "/#/auth/google/success?token=" + jwtToken;
            response.sendRedirect(redirectUrl);

            log.info("Google OAuth callback processed successfully for user: {}", user.getEmail());

        } catch (Exception e) {
            log.error("Error processing Google OAuth callback", e);

            // Redirect to frontend with error
            String errorUrl = frontendUrl + "/#/auth/google/error?message=" + e.getMessage();
            response.sendRedirect(errorUrl);
        }
    }

    /**
     * Xử lý Google ID Token từ frontend (alternative approach)
     */
    @PostMapping("/verify")
    @Operation(summary = "Verify Google ID Token", description = "Verify Google ID token and authenticate user")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyGoogleToken(
            @Parameter(description = "Google ID token", required = true)
            @RequestBody GoogleAuthDto googleAuthDto) {

        try {
            log.info("Verifying Google ID token");

            // Verify Google ID token
            GoogleUserProfileDto googleProfile = googleOAuthService.verifyIdToken(googleAuthDto.getIdToken());

            // Create or update user
            User user = userService.createOrUpdateGoogleUser(googleProfile);

            // Generate JWT token
            String jwtToken = jwtUtil.generateToken(user);

            // Create response
            UserDto userDto = new UserDto(user);
            LoginResponse loginResponse = new LoginResponse(jwtToken, userDto);

            ApiResponse<LoginResponse> apiResponse = ApiResponse.success(
                    "Đăng nhập Google thành công",
                    loginResponse
            );

            log.info("Google token verification successful for user: {}", user.getEmail());

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            log.error("Error verifying Google token", e);

            ApiResponse<LoginResponse> apiResponse = ApiResponse.error(
                    "Xác thực Google thất bại",
                    e.getMessage()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Test Google OAuth configuration
     */
    @GetMapping("/test")
    @Operation(summary = "Test Google OAuth config", description = "Test Google OAuth configuration")
    public ResponseEntity<ApiResponse<String>> testGoogleConfig() {
        try {
            String authUrl = googleOAuthService.getAuthorizationUrl();

            return ResponseEntity.ok(ApiResponse.success(
                    "Google OAuth configuration is working",
                    "Auth URL: " + authUrl
            ));

        } catch (Exception e) {
            log.error("Google OAuth configuration test failed", e);

            return new ResponseEntity<>(
                    ApiResponse.error("Google OAuth configuration failed", e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}