package com.skillbridge.skillbridge_backend.Service;

import com.skillbridge.skillbridge_backend.entity.Category;
import com.skillbridge.skillbridge_backend.entity.ListeningLesson;
import com.skillbridge.skillbridge_backend.entity.User;
import com.skillbridge.skillbridge_backend.repository.CategoryRepository;
import com.skillbridge.skillbridge_backend.repository.ListeningLessonRepository;
import com.skillbridge.skillbridge_backend.repository.UserRepository;
import com.skillbridge.skillbridge_backend.dto.ListeningLessonCreateDto;
import com.skillbridge.skillbridge_backend.exception.UserNotFoundException;
import com.skillbridge.skillbridge_backend.exception.CategoryNotFoundException;
import com.skillbridge.skillbridge_backend.exception.LessonNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
public class ListeningLessonService {

    private final ListeningLessonRepository lessonRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public ListeningLessonService(ListeningLessonRepository lessonRepository,
                                  CategoryRepository categoryRepository,
                                  UserRepository userRepository) {
        this.lessonRepository = lessonRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
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
}