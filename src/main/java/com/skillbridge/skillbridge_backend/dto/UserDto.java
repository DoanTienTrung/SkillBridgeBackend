package com.skillbridge.skillbridge_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.skillbridge.skillbridge_backend.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String email;
    private String fullName;
    private String role; // String thay vì enum để dễ serialize
    private String avatarUrl;
    private String school;
    private String major;
    private String academicYear;
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // Constructor to convert from Entity
    public UserDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.role = user.getRole().name(); // Convert enum to string
        this.avatarUrl = user.getAvatarUrl();
        this.school = user.getSchool();
        this.major = user.getMajor();
        this.academicYear = user.getAcademicYear();
        this.isActive = user.getIsActive();
        this.createdAt = user.getCreatedAt();
    }

    // Static factory method
    public static UserDto fromEntity(User user) {
        return new UserDto(user);
    }
}