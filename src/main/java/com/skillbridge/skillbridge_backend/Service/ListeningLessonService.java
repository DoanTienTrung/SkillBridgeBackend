package com.skillbridge.skillbridge_backend.Service;

import com.skillbridge.skillbridge_backend.dto.*;
import com.skillbridge.skillbridge_backend.entity.Category;
import com.skillbridge.skillbridge_backend.entity.ListeningLesson;
import com.skillbridge.skillbridge_backend.entity.Question;
import com.skillbridge.skillbridge_backend.entity.User;
import com.skillbridge.skillbridge_backend.repository.CategoryRepository;
import com.skillbridge.skillbridge_backend.repository.ListeningLessonRepository;
import com.skillbridge.skillbridge_backend.repository.UserRepository;
import com.skillbridge.skillbridge_backend.repository.QuestionRepository;
import com.skillbridge.skillbridge_backend.exception.UserNotFoundException;
import com.skillbridge.skillbridge_backend.exception.CategoryNotFoundException;
import com.skillbridge.skillbridge_backend.exception.LessonNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ListeningLessonService {


    private final ListeningLessonRepository lessonRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final VocabularyService vocabularyService;
    private final QuestionRepository questionRepository;

    public ListeningLessonService(ListeningLessonRepository lessonRepository,
                                  CategoryRepository categoryRepository,
                                  UserRepository userRepository,
                                  VocabularyService vocabularyService,
                                  QuestionRepository questionRepository) {
        this.lessonRepository = lessonRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.vocabularyService = vocabularyService;
        this.questionRepository = questionRepository;
    }

    public ListeningLesson createLesson(ListeningLessonCreateDto createDto, Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy giáo viên"));

        Category category = categoryRepository.findById(createDto.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Không tìm thấy danh mục"));

        ListeningLesson lesson = new ListeningLesson();
        lesson.setTitle(createDto.getTitle());
        lesson.setDescription(createDto.getDescription());
        lesson.setLevel(createDto.getLevel());
        lesson.setCategory(category);
        lesson.setAudioUrl(createDto.getAudioUrl());
        lesson.setTranscript(createDto.getTranscript());
        lesson.setDurationSeconds(createDto.getDurationSeconds());
        lesson.setCreatedBy(teacher);
        lesson.setStatus(ListeningLesson.Status.DRAFT);

        return lessonRepository.save(lesson);
    }

    public List<ListeningLesson> getPublishedLessons() {
        return lessonRepository.findByStatus(ListeningLesson.Status.PUBLISHED);
    }

    public List<ListeningLesson> getLessonsByLevelAndCategory(ListeningLesson.Level level, Long categoryId) {
        return lessonRepository.findPublishedByLevelAndCategory(level, categoryId);
    }

    public ListeningLesson publishLesson(Long lessonId) {
        ListeningLesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Không tìm thấy bài học"));

        lesson.setStatus(ListeningLesson.Status.PUBLISHED);
        return lessonRepository.save(lesson);
    }

    public ListeningLesson findById(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Không tìm thấy bài học"));
    }

    public LessonPreviewDto getPreviewData(Long lessonId) {
        ListeningLesson lesson = findById(lessonId);

        LessonPreviewDto preview = new LessonPreviewDto();
        preview.setId(lesson.getId());
        preview.setTitle(lesson.getTitle());
        preview.setDescription(lesson.getDescription());
        preview.setLevel(lesson.getLevel().getDisplayName());
        preview.setCategoryName(lesson.getCategory().getName());
        preview.setAudioUrl(lesson.getAudioUrl());
        preview.setTranscript(lesson.getTranscript());
        preview.setDurationSeconds(lesson.getDurationSeconds());
        preview.setStatus(lesson.getStatus().getDisplayName());
        preview.setCreatedBy(lesson.getCreatedBy().getEmail());
        preview.setCreatedAt(lesson.getCreatedAt().toString());

        // Get questions
        List<Question> questions = questionRepository.findByListeningLessonId(lessonId);
        List<QuestionDto> questionDtos = questions.stream()
                .map(this::convertQuestionToDto)
                .collect(Collectors.toList());
        preview.setQuestions(questionDtos);

        // Get vocabularies
        List<LessonVocabularyDto> vocabs = vocabularyService.getLessonVocabularies(lessonId);
        preview.setVocabularies(vocabs);

        return preview;
    }

    public List<String> validateLessonForPublish(Long lessonId) {
        List<String> errors = new ArrayList<>();
        ListeningLesson lesson = findById(lessonId);

        if (lesson.getTitle() == null || lesson.getTitle().trim().isEmpty()) {
            errors.add("Tiêu đề không được để trống");
        }

        if (lesson.getAudioUrl() == null || lesson.getAudioUrl().trim().isEmpty()) {
            errors.add("Cần upload file audio");
        }

        if (lesson.getTranscript() == null || lesson.getTranscript().trim().isEmpty()) {
            errors.add("Transcript không được để trống");
        }

        if (lesson.getCategory() == null) {
            errors.add("Cần chọn thể loại");
        }

        // Check questions
        List<Question> questions = questionRepository.findByListeningLessonId(lessonId);
        if (questions.isEmpty()) {
            errors.add("Cần có ít nhất 1 câu hỏi");
        }

        return errors;
    }

    private QuestionDto convertQuestionToDto(Question question) {
        QuestionDto dto = new QuestionDto();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionType(question.getQuestionType().toString());
        dto.setOptionA(question.getOptionA());
        dto.setOptionB(question.getOptionB());
        dto.setOptionC(question.getOptionC());
        dto.setOptionD(question.getOptionD());
        dto.setCorrectAnswer(question.getCorrectAnswer());
        dto.setExplanation(question.getExplanation());
        dto.setPoints(question.getPoints());
        return dto;
    }

    // 1. Lấy tất cả bài học cho admin/teacher (bao gồm DRAFT)
    public List<ListeningLesson> getAllLessonsForAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng"));

        // Nếu là admin -> lấy tất cả
        if (user.getRole() == User.Role.ADMIN) {
            return lessonRepository.findAllByOrderByCreatedAtDesc();
        }
        // Nếu là teacher -> chỉ lấy bài của mình
        else {
            return lessonRepository.findByCreatedByOrderByCreatedAtDesc(user);
        }
    }

    // 2. Cập nhật bài học
    public ListeningLesson updateLesson(Long lessonId, ListeningLessonUpdateDto updateDto, Long userId) {
        ListeningLesson lesson = findById(lessonId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng"));

        // Kiểm tra quyền: chỉ creator hoặc admin mới được sửa
        if (!lesson.getCreatedBy().getId().equals(userId) && !user.getRole().equals(User.Role.ADMIN)) {
            throw new SecurityException("Không có quyền chỉnh sửa bài học này");
        }

        // Cập nhật thông tin
        if (updateDto.getTitle() != null) lesson.setTitle(updateDto.getTitle());
        if (updateDto.getDescription() != null) lesson.setDescription(updateDto.getDescription());
        if (updateDto.getLevel() != null) lesson.setLevel(updateDto.getLevel());
        if (updateDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(updateDto.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Không tìm thấy danh mục"));
            lesson.setCategory(category);
        }
        if (updateDto.getTranscript() != null) lesson.setTranscript(updateDto.getTranscript());
        if (updateDto.getDurationSeconds() != null) lesson.setDurationSeconds(updateDto.getDurationSeconds());

        return lessonRepository.save(lesson);
    }

    // 3. Xóa bài học (soft delete)
    public boolean deleteLesson(Long lessonId, Long userId) {
        ListeningLesson lesson = findById(lessonId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng"));

        // Kiểm tra quyền
        if (!lesson.getCreatedBy().getId().equals(userId) && !user.getRole().equals(User.Role.ADMIN)) {
            throw new SecurityException("Không có quyền xóa bài học này");
        }

        // Soft delete: thêm field deleted vào entity (khuyến nghị)
        // Hoặc hard delete nếu chắc chắn
        lessonRepository.delete(lesson);
        return true;
    }

    // 4. Thay đổi trạng thái
    public ListeningLesson updateLessonStatus(Long lessonId, ListeningLesson.Status newStatus, Long userId) {
        ListeningLesson lesson = findById(lessonId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng"));

        // Kiểm tra quyền
        if (!lesson.getCreatedBy().getId().equals(userId) && !user.getRole().equals(User.Role.ADMIN)) {
            throw new SecurityException("Không có quyền thay đổi trạng thái bài học này");
        }

        // Nếu chuyển sang PUBLISHED, validate trước
        if (newStatus == ListeningLesson.Status.PUBLISHED) {
            List<String> validationErrors = validateLessonForPublish(lessonId);
            if (!validationErrors.isEmpty()) {
                throw new IllegalStateException("Bài học chưa đủ điều kiện xuất bản: " +
                        String.join(", ", validationErrors));
            }
        }

        lesson.setStatus(newStatus);
        return lessonRepository.save(lesson);
    }
}