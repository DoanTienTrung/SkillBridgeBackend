package com.skillbridge.skillbridgebackend.repository;

import com.skillbridge.skillbridgebackend.entity.PasswordResetToken;
import com.skillbridge.skillbridgebackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Tìm token theo string token
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Tìm token chưa sử dụng theo user
     */
    Optional<PasswordResetToken> findByUserAndIsUsedFalse(User user);

    /**
     * Xóa tất cả token hết hạn
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiryDate < :currentTime")
    void deleteExpiredTokens(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Đánh dấu tất cả token của user là đã sử dụng
     */
    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetToken p SET p.isUsed = true WHERE p.user = :user")
    void markAllTokensAsUsedForUser(@Param("user") User user);

    /**
     * Đếm số token valid của user
     */
    @Query("SELECT COUNT(p) FROM PasswordResetToken p WHERE p.user = :user AND p.isUsed = false AND p.expiryDate > :currentTime")
    long countValidTokensForUser(@Param("user") User user, @Param("currentTime") LocalDateTime currentTime);
}