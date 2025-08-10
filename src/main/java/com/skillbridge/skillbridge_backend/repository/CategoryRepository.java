package com.skillbridge.skillbridge_backend.repository;

import com.skillbridge.skillbridge_backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Tìm category theo tên
     */
    Optional<Category> findByName(String name);

    /**
     * Tìm category theo tên (không phân biệt hoa thường)
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Kiểm tra category name đã tồn tại
     */
    boolean existsByName(String name);

    /**
     * Tìm categories có description
     */
    List<Category> findByDescriptionIsNotNull();

    /**
     * Custom query - Đếm số bài học trong mỗi category
     */
    @Query("SELECT c.name, " +
            "(SELECT COUNT(l) FROM ListeningLesson l WHERE l.category = c) + " +
            "(SELECT COUNT(r) FROM ReadingLesson r WHERE r.category = c) " +
            "FROM Category c")
    List<Object[]> countLessonsPerCategory();
}