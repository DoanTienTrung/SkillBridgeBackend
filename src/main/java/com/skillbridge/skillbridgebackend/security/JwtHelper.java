package com.skillbridge.skillbridgebackend.security;

import com.skillbridge.skillbridgebackend.entity.User;
import com.skillbridge.skillbridgebackend.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JwtHelper {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    /**
     * Get current authenticated user from Security Context
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            return userService.findByEmail(email);
        }
        return null;
    }

    /**
     * Get current user ID from JWT token
     */
    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Get user from JWT token in request header
     */
    public User getUserFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String email = jwtUtil.getEmailFromToken(token);
                if (jwtUtil.validateToken(token, email)) {
                    return userService.findByEmail(email);
                }
            } catch (Exception e) {
                // Invalid token
            }
        }
        return null;
    }

    /**
     * Extract JWT token from request header
     */
    public String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Check if user has specific role
     */
    public boolean hasRole(String role) {
        User user = getCurrentUser();
        return user != null && user.getRole().name().equals(role);
    }

    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if user is teacher
     */
    public boolean isTeacher() {
        return hasRole("TEACHER");
    }

    /**
     * Check if user is student
     */
    public boolean isStudent() {
        return hasRole("STUDENT");
    }
}
