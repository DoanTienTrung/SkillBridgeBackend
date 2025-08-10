package com.skillbridge.skillbridge_backend.Service;

import com.skillbridge.skillbridge_backend.dto.UserRegistrationDto;
import com.skillbridge.skillbridge_backend.entity.User;
import com.skillbridge.skillbridge_backend.exception.EmailAlreadyExistsException;
import com.skillbridge.skillbridge_backend.exception.UserNotFoundException;
import com.skillbridge.skillbridge_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Đăng ký user mới
     */
    public User registerUser(UserRegistrationDto registrationDto) {
        // Check if email already exists
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new EmailAlreadyExistsException("Email đã được sử dụng: " + registrationDto.getEmail());
        }

        // Create new user
        User user = new User();
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFullName(registrationDto.getFullName());
        user.setRole(User.Role.STUDENT); // Default role
        user.setSchool(registrationDto.getSchool());
        user.setMajor(registrationDto.getMajor());
        user.setAcademicYear(registrationDto.getAcademicYear());
        user.setIsActive(true);

        return userRepository.save(user);
    }

    /**
     * Tìm user theo email
     */
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với email: " + email));
    }

    /**
     * Tìm user theo ID
     */
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với ID: " + id));
    }

    /**
     * Cập nhật thông tin user
     */
    public User updateUser(Long userId, UserRegistrationDto updateDto) {
        User user = findById(userId);

        // Update fields
        user.setFullName(updateDto.getFullName());
        user.setSchool(updateDto.getSchool());
        user.setMajor(updateDto.getMajor());
        user.setAcademicYear(updateDto.getAcademicYear());

        return userRepository.save(user);
    }

    /**
     * Đổi mật khẩu
     */
    public void changePassword(Long userId, String newPassword) {
        User user = findById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Lấy tất cả học viên
     */
    @Transactional(readOnly = true)
    public List<User> getAllStudents() {
        return userRepository.findActiveUsersByRole(User.Role.STUDENT);
    }

    /**
     * Lấy tất cả giáo viên
     */
    @Transactional(readOnly = true)
    public List<User> getAllTeachers() {
        return userRepository.findActiveUsersByRole(User.Role.TEACHER);
    }

    /**
     * Kích hoạt/vô hiệu hóa user
     */
    public User toggleUserActive(Long userId) {
        User user = findById(userId);
        user.setIsActive(!user.getIsActive());
        return userRepository.save(user);
    }
}