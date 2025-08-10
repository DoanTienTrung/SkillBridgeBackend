package com.skillbridge.skillbridge_backend.repository;

import com.skillbridge.skillbridge_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ===== QUERY METHODS BY CONVENTION =====

    /**
     * Tìm user theo email
     * Spring tự động tạo query: SELECT * FROM users WHERE email = ?
     */
    Optional<User> findByEmail(String email);

    /**
     * Tìm user theo role
     * Spring tự động tạo query: SELECT * FROM users WHERE role = ?
     */
    List<User> findByRole(User.Role role);

    /**
     * Kiểm tra email đã tồn tại chưa
     * Spring tự động tạo query: SELECT COUNT(*) > 0 FROM users WHERE email = ?
     */
    boolean existsByEmail(String email);

    /**
     * Tìm user theo tên (không phân biệt hoa thường)
     * Spring tự động tạo query: SELECT * FROM users WHERE UPPER(full_name) LIKE UPPER(?%)
     */
    List<User> findByFullNameContainingIgnoreCase(String fullName);

    /**
     * Tìm user theo role và trạng thái active
     */
    List<User> findByRoleAndIsActive(User.Role role, Boolean isActive);

    // ===== CUSTOM QUERIES WITH @Query =====

    /**
     * Tìm user active theo role (dùng JPQL)
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true")
    List<User> findActiveUsersByRole(@Param("role") User.Role role);

    /**
     * Đếm số học viên theo trường
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.school = :school AND u.role = 'STUDENT'")
    Long countStudentsBySchool(@Param("school") String school);

    /**
     * Tìm top 10 user mới nhất
     */
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC LIMIT 10")
    List<User> findTop10NewestUsers();

    /**
     * Native SQL query - Tìm user theo email domain
     */
    @Query(value = "SELECT * FROM users WHERE email LIKE %:domain", nativeQuery = true)
    List<User> findByEmailDomain(@Param("domain") String domain);
}