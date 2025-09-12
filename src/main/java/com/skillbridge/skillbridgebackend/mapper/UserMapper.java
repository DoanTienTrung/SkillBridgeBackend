package com.skillbridge.skillbridgebackend.mapper;

import com.skillbridge.skillbridgebackend.dto.UserDto;
import com.skillbridge.skillbridgebackend.dto.UserRegistrationDto;
import com.skillbridge.skillbridgebackend.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    /**
     * Convert từ User Entity sang UserDto
     */
    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());

        // Convert enum sang String
        dto.setRole(user.getRole().name()); // Fix: Chuyển enum thành String

        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setSchool(user.getSchool());
        dto.setMajor(user.getMajor());
        dto.setAcademicYear(user.getAcademicYear());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());

        return dto;
    }

    /**
     * Convert từ UserRegistrationDto sang User Entity
     */
    public static User toEntity(UserRegistrationDto registrationDto) {
        if (registrationDto == null) {
            return null;
        }

        User user = new User();
        user.setEmail(registrationDto.getEmail());
        user.setFullName(registrationDto.getFullName());
        user.setSchool(registrationDto.getSchool());
        user.setMajor(registrationDto.getMajor());
        user.setAcademicYear(registrationDto.getAcademicYear());
        user.setRole(User.Role.STUDENT); // Default role
        user.setIsActive(true); // Default active

        // Password sẽ được encode trong service layer
        // user.setPassword() sẽ được set trong UserService

        return user;
    }

    /**
     * Update User entity từ DTO (không update password và role)
     */
    public static void updateEntityFromDto(User user, UserRegistrationDto updateDto) {
        if (user == null || updateDto == null) {
            return;
        }

        if (updateDto.getFullName() != null) {
            user.setFullName(updateDto.getFullName());
        }

        if (updateDto.getSchool() != null) {
            user.setSchool(updateDto.getSchool());
        }

        if (updateDto.getMajor() != null) {
            user.setMajor(updateDto.getMajor());
        }

        if (updateDto.getAcademicYear() != null) {
            user.setAcademicYear(updateDto.getAcademicYear());
        }
    }

    /**
     * Convert list User entities sang list UserDto
     */
    public static List<UserDto> toDtoList(List<User> users) {
        if (users == null) {
            return null;
        }

        return users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert sang UserDto với thông tin cơ bản (không có sensitive data)
     */
    public static UserDto toPublicDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole().name());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setSchool(user.getSchool());
        dto.setIsActive(user.getIsActive());

        // Không expose email, major, academicYear cho public

        return dto;
    }

    /**
     * Convert User Role enum sang String với display name
     */
    public static String getRoleDisplayName(User.Role role) {
        if (role == null) {
            return null;
        }

        switch (role) {
            case STUDENT:
                return "Học viên";
            case TEACHER:
                return "Giáo viên";
            case ADMIN:
                return "Quản trị viên";
            default:
                return role.name();
        }
    }

    /**
     * Convert String sang User Role enum
     */
    public static User.Role parseRole(String roleString) {
        if (roleString == null || roleString.trim().isEmpty()) {
            return User.Role.STUDENT; // Default
        }

        try {
            return User.Role.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return User.Role.STUDENT; // Default nếu không parse được
        }
    }
}