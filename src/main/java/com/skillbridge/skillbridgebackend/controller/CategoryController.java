package com.skillbridge.skillbridgebackend.controller;

import com.skillbridge.skillbridgebackend.entity.Category;
import com.skillbridge.skillbridgebackend.repository.CategoryRepository;
import com.skillbridge.skillbridgebackend.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@CrossOrigin(origins = "*")
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    @Operation(summary = "Get all categories", description = "Get list of all categories")
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        try {
            List<Category> categories = categoryRepository.findAll();
            return ResponseEntity.ok(
                    ApiResponse.success("Lấy danh sách thể loại thành công", categories)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy danh sách thể loại: " + e.getMessage()));
        }
    }
}