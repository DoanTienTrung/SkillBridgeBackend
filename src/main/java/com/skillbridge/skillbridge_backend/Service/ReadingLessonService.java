// Tạo file Service/ReadingLessonService.java
package com.skillbridge.skillbridge_backend.Service;

import com.skillbridge.skillbridge_backend.dto.ReadingLessonCreateDto;
import com.skillbridge.skillbridge_backend.entity.*;
import com.skillbridge.skillbridge_backend.repository.*;
import com.skillbridge.skillbridge_backend.exception.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class ReadingLessonService {

    private final ReadingLessonRepository readingLessonRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;

    public ReadingLessonService(ReadingLessonRepository readingLessonRepository,
                                CategoryRepository categoryRepository,
                                UserRepository userRepository,
                                QuestionRepository questionRepository) {
        this.readingLessonRepository = readingLessonRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
    }

    // Tạo bài đọc mới
    public ReadingLesson createReadingLesson(ReadingLessonCreateDto createDto, Long teacherId) {
        // Validate user
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy giáo viên"));

        // Validate category
        Category category = categoryRepository.findById(createDto.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Không tìm thấy danh mục"));

        // Tạo reading lesson
        ReadingLesson lesson = new ReadingLesson();
        lesson.setTitle(createDto.getTitle());
        lesson.setDescription(createDto.getDescription());
        lesson.setLevel(createDto.getLevel());
        lesson.setCategory(category);
        lesson.setContent(createDto.getContent());
        lesson.setWordCount(calculateWordCount(createDto.getContent()));
        lesson.setCreatedBy(teacher);
        lesson.setStatus(ListeningLesson.Status.DRAFT);

        return readingLessonRepository.save(lesson);
    }

    // Tính số từ trong content
    private Integer calculateWordCount(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }

        // Remove HTML tags nếu có
        String plainText = content.replaceAll("<[^>]+>", "");

        // Split by whitespace và đếm từ
        String[] words = plainText.trim().split("\\s+");
        return words.length;
    }

    // Lấy tất cả bài đọc cho admin/teacher
    public List<ReadingLesson> getAllReadingLessonsForAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng"));

        if (user.getRole() == User.Role.ADMIN) {
            return readingLessonRepository.findAll();
        } else {
            return readingLessonRepository.findByCreatedById(userId);
        }
    }

    // Lấy bài published
    public List<ReadingLesson> getPublishedReadingLessons() {
        return readingLessonRepository.findByStatus(ListeningLesson.Status.PUBLISHED);
    }

    // Lấy chi tiết bài đọc
    public ReadingLesson findById(Long lessonId) {
        return readingLessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Không tìm thấy bài đọc"));
    }

    // Cập nhật bài đọc
    public ReadingLesson updateReadingLesson(Long lessonId, ReadingLessonCreateDto updateDto, Long userId) {
        ReadingLesson lesson = findById(lessonId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng"));

        // Kiểm tra quyền
        if (!lesson.getCreatedBy().getId().equals(userId) && !user.getRole().equals(User.Role.ADMIN)) {
            throw new SecurityException("Không có quyền chỉnh sửa bài đọc này");
        }

        // Cập nhật thông tin
        lesson.setTitle(updateDto.getTitle());
        lesson.setDescription(updateDto.getDescription());
        lesson.setLevel(updateDto.getLevel());
        lesson.setContent(updateDto.getContent());
        lesson.setWordCount(calculateWordCount(updateDto.getContent()));

        if (updateDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(updateDto.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Không tìm thấy danh mục"));
            lesson.setCategory(category);
        }

        return readingLessonRepository.save(lesson);
    }

    // Xóa bài đọc
    public boolean deleteReadingLesson(Long lessonId, Long userId) {
        ReadingLesson lesson = findById(lessonId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng"));

        // Kiểm tra quyền
        if (!lesson.getCreatedBy().getId().equals(userId) && !user.getRole().equals(User.Role.ADMIN)) {
            throw new SecurityException("Không có quyền xóa bài đọc này");
        }

        readingLessonRepository.delete(lesson);
        return true;
    }

    // Thay đổi trạng thái
    public ReadingLesson updateReadingLessonStatus(Long lessonId, ListeningLesson.Status newStatus, Long userId) {
        ReadingLesson lesson = findById(lessonId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng"));

        // Kiểm tra quyền
        if (!lesson.getCreatedBy().getId().equals(userId) && !user.getRole().equals(User.Role.ADMIN)) {
            throw new SecurityException("Không có quyền thay đổi trạng thái bài đọc này");
        }

        // Validate trước khi publish
        if (newStatus == ListeningLesson.Status.PUBLISHED) {
            List<String> validationErrors = validateReadingLessonForPublish(lessonId);
            if (!validationErrors.isEmpty()) {
                throw new IllegalStateException("Bài đọc chưa đủ điều kiện xuất bản: " +
                        String.join(", ", validationErrors));
            }
        }

        lesson.setStatus(newStatus);
        return readingLessonRepository.save(lesson);
    }

    // Validate bài đọc trước khi publish
    public List<String> validateReadingLessonForPublish(Long lessonId) {
        List<String> errors = new ArrayList<>();
        ReadingLesson lesson = findById(lessonId);

        if (lesson.getTitle() == null || lesson.getTitle().trim().isEmpty()) {
            errors.add("Tiêu đề không được để trống");
        }

        if (lesson.getContent() == null || lesson.getContent().trim().isEmpty()) {
            errors.add("Nội dung không được để trống");
        } else if (lesson.getWordCount() < 50) {
            errors.add("Nội dung quá ngắn (cần ít nhất 50 từ)");
        }

        if (lesson.getCategory() == null) {
            errors.add("Cần chọn thể loại");
        }

        // Check questions
        List<Question> questions = questionRepository.findByReadingLessonId(lessonId);
        if (questions.isEmpty()) {
            errors.add("Cần có ít nhất 1 câu hỏi");
        }

        return errors;
    }

    // Upload text file
    public String processTextFile(byte[] fileContent) {
        try {
            String content = new String(fileContent, "UTF-8");

            // Basic text cleaning
            content = content.trim();
            content = content.replaceAll("\\r\\n", "\n"); // Normalize line endings
            content = content.replaceAll("\\r", "\n");

            return content;
        } catch (Exception e) {
            throw new IllegalArgumentException("Không thể đọc file text: " + e.getMessage());
        }
    }
}