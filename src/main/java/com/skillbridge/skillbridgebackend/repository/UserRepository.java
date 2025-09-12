package com.skillbridge.skillbridgebackend.repository;

import com.skillbridge.skillbridgebackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    // ===== ANALYTICS METHODS =====

    /**
     * Đếm tổng số users
     */
    @Query("SELECT COUNT(u) FROM User u")
    Integer countAll();

    /**
     * Đếm users theo role
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    Integer countByRole(@Param("role") User.Role role);

    /**
     * Đếm users đăng ký sau một thời điểm
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    Integer countByCreatedAtAfter(@Param("since") LocalDateTime since);

    /**
     * Đếm users đăng ký trong khoảng thời gian và theo role
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :start AND :end AND u.role = :role")
    Integer countByCreatedAtBetweenAndRole(@Param("start") LocalDateTime start, 
                                           @Param("end") LocalDateTime end, 
                                           @Param("role") User.Role role);

    /**
     * Tìm users đăng ký trong khoảng thời gian
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :start AND :end ORDER BY u.createdAt DESC")
    List<User> findByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Đếm users active theo khoảng thời gian (có hoạt động trong user_lesson_progress)
     */
    @Query(value = "SELECT COUNT(DISTINCT u.id) FROM users u " +
                   "INNER JOIN user_lesson_progress ulp ON u.id = ulp.user_id " +
                   "WHERE ulp.created_at BETWEEN :start AND :end", nativeQuery = true)
    Integer countActiveUsersByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // ===== ADDITIONAL ANALYTICS METHODS =====

    /**
     * Đếm users theo role và trạng thái active
     */
    Integer countByRoleAndIsActiveTrue(User.Role role);

    /**
     * Đếm tất cả users active
     */
    Integer countByIsActiveTrue();

    /**
     * Đếm users đăng ký trong khoảng thời gian
     */
    Integer countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Tìm users theo role và active
     */
    List<User> findByRoleAndIsActiveTrue(User.Role role);
}