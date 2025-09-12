package com.skillbridge.skillbridgebackend.Service;

import com.skillbridge.skillbridgebackend.dto.UserRegistrationDto;
import com.skillbridge.skillbridgebackend.dto.*;
import com.skillbridge.skillbridgebackend.entity.User;
import com.skillbridge.skillbridgebackend.entity.*;
import com.skillbridge.skillbridgebackend.exception.EmailAlreadyExistsException;
import com.skillbridge.skillbridgebackend.exception.UserNotFoundException;
import com.skillbridge.skillbridgebackend.repository.UserRepository;
import com.skillbridge.skillbridgebackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.skillbridge.skillbridgebackend.dto.UserProfileUpdateDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Student-related repositories
    @Autowired
    private ListeningLessonRepository listeningLessonRepository;

    @Autowired
    private ReadingLessonRepository readingLessonRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserAnswerRepository userAnswerRepository;

    @Autowired
    private UserLessonProgressRepository progressRepository;

    @Autowired
    private UserVocabularyRepository userVocabularyRepository;

    /**
     * Đăng ký user mới
     */
    public User registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new EmailAlreadyExistsException("Email đã được sử dụng: " + registrationDto.getEmail());
        }

        User user = new User();
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFullName(registrationDto.getFullName());

        // ✅ Nếu request có role thì set, nếu không mặc định STUDENT
        if (registrationDto.getRole() != null && !registrationDto.getRole().isEmpty()) {
            try {
                user.setRole(User.Role.valueOf(registrationDto.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Role không hợp lệ: " + registrationDto.getRole());
            }
        } else {
            user.setRole(User.Role.STUDENT);
        }

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

        // Update role if provided (for admin operations)
        if (updateDto.getRole() != null && !updateDto.getRole().isEmpty()) {
            user.setRole(User.Role.valueOf(updateDto.getRole().toUpperCase()));
        }

        // Update password if provided
        if (updateDto.getPassword() != null && !updateDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }

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

    // ===== STUDENT-SPECIFIC METHODS =====

    /**
     * Get student statistics for dashboard
     */
    public StudentStatsDto getStudentStats(Long userId) {
        log.info("Getting student stats for user: {}", userId);

        StudentStatsDto stats = new StudentStatsDto();

        try {
            // Count completed lessons
            Long completedLessons = progressRepository.countCompletedLessonsByUser(userId);
            stats.setCompletedLessons(completedLessons != null ? completedLessons.intValue() : 0);

            // Count total available published lessons
            long totalListening = listeningLessonRepository.countByStatus(ListeningLesson.Status.PUBLISHED);
            long totalReading = readingLessonRepository.countByStatus(ListeningLesson.Status.PUBLISHED);
            stats.setTotalLessons((int) (totalListening + totalReading));

            // Calculate average score
            Double avgScore = progressRepository.getAverageScoreByUser(userId);
            stats.setAverageScore(avgScore != null ? avgScore : 0.0);

            // Calculate total time studied (convert from Long to Integer)
            Long totalTime = progressRepository.getTotalTimeSpentByUser(userId);
            stats.setTotalTimeStudied(totalTime != null ? totalTime.intValue() : 0);

            // Count vocabulary learned
            long vocabCount = userVocabularyRepository.countByUserId(userId);
            stats.setVocabularyCount((int) vocabCount);

            // Get weekly progress (simple implementation for now)
            List<UserLessonProgress> recentProgress = progressRepository.findByUserIdAndIsCompleted(userId, true);
            stats.setWeeklyProgress(generateWeeklyProgressData(recentProgress));

            log.info("Student stats retrieved successfully: {}", stats);
            return stats;

        } catch (Exception e) {
            log.error("Error getting student stats for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get student statistics", e);
        }
    }

    /**
     * Get recent lessons for student
     */
    public List<RecentLessonDto> getRecentLessons(Long userId, int limit) {
        log.info("Getting recent lessons for user: {}, limit: {}", userId, limit);

        try {
            List<UserLessonProgress> recentProgress = progressRepository.findByUserId(userId);

            return recentProgress.stream()
                    .sorted((a, b) -> {
                        LocalDateTime timeA = a.getCompletedAt() != null ? a.getCompletedAt() : a.getCreatedAt();
                        LocalDateTime timeB = b.getCompletedAt() != null ? b.getCompletedAt() : b.getCreatedAt();
                        return timeB.compareTo(timeA); // Latest first
                    })
                    .limit(limit)
                    .map(this::convertToRecentLessonDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting recent lessons for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get recent lessons", e);
        }
    }

    /**
     * Get published lessons with filtering
     */
    public List<LessonDto> getPublishedLessons(String type, String level, Long categoryId, String search) {
        log.info("Getting published lessons with filters - type: {}, level: {}, categoryId: {}, search: {}",
                type, level, categoryId, search);

        try {
            List<LessonDto> allLessons = new ArrayList<>();

            // Get Reading Lessons
            if (type == null || "all".equals(type) || "READING".equalsIgnoreCase(type) || "reading".equalsIgnoreCase(type)) {
                List<ReadingLesson> readingLessons = readingLessonRepository.findByStatus(ListeningLesson.Status.PUBLISHED);

                for (ReadingLesson lesson : readingLessons) {
                    // Apply filters
                    if (level != null && !level.equals("all") && !lesson.getLevel().name().equalsIgnoreCase(level)) {
                        continue;
                    }
                    if (categoryId != null && (lesson.getCategory() == null || !lesson.getCategory().getId().equals(categoryId))) {
                        continue;
                    }
                    if (search != null && !search.trim().isEmpty()) {
                        String searchLower = search.toLowerCase();
                        if (!lesson.getTitle().toLowerCase().contains(searchLower) &&
                                !lesson.getDescription().toLowerCase().contains(searchLower)) {
                            continue;
                        }
                    }

                    LessonDto dto = convertToLessonDto(lesson, "reading");
                    allLessons.add(dto);
                }
            }

            // Get Listening Lessons
            if (type == null || "all".equals(type) || "LISTENING".equalsIgnoreCase(type) || "listening".equalsIgnoreCase(type)) {
                List<ListeningLesson> listeningLessons = listeningLessonRepository.findByStatus(ListeningLesson.Status.PUBLISHED);

                for (ListeningLesson lesson : listeningLessons) {
                    // Apply filters
                    if (level != null && !level.equals("all") && !lesson.getLevel().name().equalsIgnoreCase(level)) {
                        continue;
                    }
                    if (categoryId != null && (lesson.getCategory() == null || !lesson.getCategory().getId().equals(categoryId))) {
                        continue;
                    }
                    if (search != null && !search.trim().isEmpty()) {
                        String searchLower = search.toLowerCase();
                        if (!lesson.getTitle().toLowerCase().contains(searchLower) &&
                                !lesson.getDescription().toLowerCase().contains(searchLower)) {
                            continue;
                        }
                    }

                    LessonDto dto = convertToLessonDto(lesson, "listening");
                    allLessons.add(dto);
                }
            }

            // Sort by creation date (newest first)
            allLessons.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

            log.info("Retrieved {} published lessons", allLessons.size());
            return allLessons;

        } catch (Exception e) {
            log.error("Error getting published lessons: {}", e.getMessage());
            throw new RuntimeException("Failed to get published lessons", e);
        }
    }

    /**
     * Get lesson for student view
     */
    public LessonDto getLessonForStudent(Long id, String type) {
        log.info("Getting lesson for student - id: {}, type: {}", id, type);

        try {
            if ("listening".equals(type)) {
                ListeningLesson lesson = listeningLessonRepository.findByIdAndStatus(id, ListeningLesson.Status.PUBLISHED)
                        .orElseThrow(() -> new RuntimeException("Lesson not found or not published"));
                return convertToLessonDto(lesson, "listening");
            } else {
                ReadingLesson lesson = readingLessonRepository.findByIdAndStatus(id, ListeningLesson.Status.PUBLISHED)
                        .orElseThrow(() -> new RuntimeException("Lesson not found or not published"));
                return convertToLessonDto(lesson, "reading");
            }
        } catch (Exception e) {
            log.error("Error getting lesson {} of type {}: {}", id, type, e.getMessage());
            throw new RuntimeException("Failed to get lesson", e);
        }
    }

    /**
     * Submit student answers and calculate score
     */
    public SubmissionResultDto submitAnswers(Long userId, SubmissionDto submission) {
        log.info("Submitting answers for user: {}, lesson: {}", userId, submission.getLessonId());

        try {
            List<Question> questions = questionRepository.findByLessonIdAndLessonType(
                    submission.getLessonId(),
                    Question.LessonType.valueOf(submission.getLessonType().toUpperCase())
            );

            int correctAnswers = 0;
            int totalQuestions = questions.size();

            User user = findById(userId);

            // Save user answers and count correct ones
            for (Question question : questions) {
                String userAnswer = submission.getAnswers().get(question.getId().toString());
                if (userAnswer != null) {
                    UserAnswer answer = new UserAnswer();
                    answer.setUser(user);
                    answer.setQuestion(question);
                    answer.setSelectedAnswer(userAnswer);
                    answer.setIsCorrect(userAnswer.equals(question.getCorrectAnswer()));

                    userAnswerRepository.save(answer);

                    if (answer.getIsCorrect()) {
                        correctAnswers++;
                    }
                }
            }

            // Calculate score
            BigDecimal score = totalQuestions > 0
                    ? BigDecimal.valueOf((double) correctAnswers / totalQuestions * 10)
                    : BigDecimal.ZERO;

            // Save or update progress
            Optional<UserLessonProgress> existingProgress = progressRepository
                    .findByUserIdAndLessonIdAndLessonType(
                            userId,
                            submission.getLessonId(),
                            Question.LessonType.valueOf(submission.getLessonType().toUpperCase())
                    );

            UserLessonProgress progress;
            if (existingProgress.isPresent()) {
                progress = existingProgress.get();
            } else {
                progress = new UserLessonProgress();
                progress.setUser(user);
                progress.setLessonId(submission.getLessonId());
                progress.setLessonType(Question.LessonType.valueOf(submission.getLessonType().toUpperCase()));
            }

            progress.setIsCompleted(true);
            progress.setScore(score);
            progress.setTimeSpentSeconds(submission.getTimeSpent());
            progress.setCompletedAt(LocalDateTime.now());

            progressRepository.save(progress);

            // Prepare result
            SubmissionResultDto result = new SubmissionResultDto();
            result.setScore(score.doubleValue());
            result.setCorrectAnswers(correctAnswers);
            result.setTotalQuestions(totalQuestions);
            result.setTimeSpent(submission.getTimeSpent());

            log.info("Submission completed - score: {}, correct: {}/{}", score, correctAnswers, totalQuestions);
            return result;

        } catch (Exception e) {
            log.error("Error submitting answers for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to submit answers", e);
        }
    }

    /**
     * Get student progress data for analytics
     */
    public StudentProgressDto getProgressData(Long userId, String timeRange) {
        log.info("Getting progress data for user: {}, timeRange: {}", userId, timeRange);

        try {
            StudentProgressDto progressData = new StudentProgressDto();

            // Get overview stats (reuse existing method)
            StudentStatsDto overview = getStudentStats(userId);
            progressData.setOverview(overview);

            // Get charts data
            Map<String, Object> charts = new HashMap<>();

            // Simple weekly progress for now
            List<UserLessonProgress> progressList = progressRepository.findByUserId(userId);
            charts.put("weeklyProgress", generateWeeklyProgressData(progressList));
            charts.put("levelProgress", generateLevelProgressData(progressList));

            progressData.setCharts(charts);

            // Get recent activities
            List<RecentLessonDto> recentActivities = getRecentLessons(userId, 10);
            progressData.setRecentActivities(recentActivities);

            return progressData;

        } catch (Exception e) {
            log.error("Error getting progress data for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get progress data", e);
        }
    }

    // Helper methods
    private LessonDto convertToLessonDto(Object lesson, String type) {
        LessonDto dto = new LessonDto();

        if ("listening".equals(type) && lesson instanceof ListeningLesson) {
            ListeningLesson listeningLesson = (ListeningLesson) lesson;
            dto.setId(listeningLesson.getId());
            dto.setTitle(listeningLesson.getTitle());
            dto.setDescription(listeningLesson.getDescription());
            dto.setLevel(listeningLesson.getLevel().name());
            dto.setType("listening");
            dto.setDurationSeconds(listeningLesson.getDurationSeconds());
            dto.setAudioUrl(listeningLesson.getAudioUrl());
            dto.setTranscript(listeningLesson.getTranscript());
            dto.setCreatedAt(listeningLesson.getCreatedAt());

            if (listeningLesson.getCategory() != null) {
                CategoryDto categoryDto = new CategoryDto();
                categoryDto.setId(listeningLesson.getCategory().getId());
                categoryDto.setName(listeningLesson.getCategory().getName());
                dto.setCategory(categoryDto);
            }
        } else if ("reading".equals(type) && lesson instanceof ReadingLesson) {
            ReadingLesson readingLesson = (ReadingLesson) lesson;
            dto.setId(readingLesson.getId());
            dto.setTitle(readingLesson.getTitle());
            dto.setDescription(readingLesson.getDescription());
            dto.setLevel(readingLesson.getLevel().name());
            dto.setType("reading");
            dto.setContent(readingLesson.getContent());
            dto.setWordCount(readingLesson.getWordCount());
            dto.setCreatedAt(readingLesson.getCreatedAt());

            if (readingLesson.getCategory() != null) {
                CategoryDto categoryDto = new CategoryDto();
                categoryDto.setId(readingLesson.getCategory().getId());
                categoryDto.setName(readingLesson.getCategory().getName());
                dto.setCategory(categoryDto);
            }
        }

        return dto;
    }

    private RecentLessonDto convertToRecentLessonDto(UserLessonProgress progress) {
        RecentLessonDto dto = new RecentLessonDto();
        dto.setLessonId(progress.getLessonId());
        dto.setLessonType(progress.getLessonType().name().toLowerCase());
        dto.setScore(progress.getScore() != null ? progress.getScore().doubleValue() : 0.0);
        dto.setTimeSpent(progress.getTimeSpentSeconds());
        dto.setCompletedAt(progress.getCompletedAt());
        dto.setIsCompleted(progress.getIsCompleted());

        // Get lesson title based on type
        try {
            if (progress.getLessonType() == Question.LessonType.LISTENING) {
                listeningLessonRepository.findById(progress.getLessonId())
                        .ifPresent(lesson -> dto.setLessonTitle(lesson.getTitle()));
            } else {
                readingLessonRepository.findById(progress.getLessonId())
                        .ifPresent(lesson -> dto.setLessonTitle(lesson.getTitle()));
            }
        } catch (Exception e) {
            dto.setLessonTitle("Unknown Lesson");
        }

        return dto;
    }

    private List<Map<String, Object>> generateWeeklyProgressData(List<UserLessonProgress> progressList) {
        // Simple implementation - group by day and calculate average score
        Map<String, List<UserLessonProgress>> groupedByDay = progressList.stream()
                .filter(p -> p.getCompletedAt() != null && p.getScore() != null)
                .collect(Collectors.groupingBy(p -> p.getCompletedAt().toLocalDate().toString()));

        return groupedByDay.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", entry.getKey());

                    double avgScore = entry.getValue().stream()
                            .mapToDouble(p -> p.getScore().doubleValue())
                            .average()
                            .orElse(0.0);

                    int totalTime = entry.getValue().stream()
                            .mapToInt(p -> p.getTimeSpentSeconds() != null ? p.getTimeSpentSeconds() : 0)
                            .sum();

                    dayData.put("score", avgScore);
                    dayData.put("timeSpent", totalTime);
                    return dayData;
                })
                .sorted((a, b) -> ((String) a.get("date")).compareTo((String) b.get("date")))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> generateLevelProgressData(List<UserLessonProgress> progressList) {
        // Count progress by lesson type
        Map<String, Long> counts = progressList.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getLessonType().name(),
                        Collectors.counting()
                ));

        return counts.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> levelData = new HashMap<>();
                    levelData.put("level", entry.getKey());
                    levelData.put("count", entry.getValue());
                    return levelData;
                })
                .collect(Collectors.toList());
    }

    // Additional methods for Admin User Management
    public List<UserDto> getAllUsers(String role) {
        List<User> users;
        if (role != null && !role.isEmpty()) {
            User.Role userRole = User.Role.valueOf(role.toUpperCase());
            users = userRepository.findByRole(userRole);
        } else {
            users = userRepository.findAll();
        }
        return users.stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
    }

    public String resetPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return newPassword;
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        userRepository.delete(user);
    }

    public User createUser(UserRegistrationDto userData) {
        // Check if email already exists
        if (userRepository.existsByEmail(userData.getEmail())) {
            throw new EmailAlreadyExistsException("Email đã được sử dụng: " + userData.getEmail());
        }

        User user = new User();
        user.setEmail(userData.getEmail());
        user.setPassword(passwordEncoder.encode(userData.getPassword()));
        user.setFullName(userData.getFullName());
        user.setSchool(userData.getSchool());
        user.setMajor(userData.getMajor());
        user.setAcademicYear(userData.getAcademicYear());
        user.setIsActive(true);

        // Set role from DTO or default to STUDENT
        if (userData.getRole() != null && !userData.getRole().isEmpty()) {
            user.setRole(User.Role.valueOf(userData.getRole().toUpperCase()));
        } else {
            user.setRole(User.Role.STUDENT);
        }

        return userRepository.save(user);
    }

    private UserDto convertToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole().name());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setSchool(user.getSchool());
        dto.setMajor(user.getMajor());
        dto.setAcademicYear(user.getAcademicYear());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    private String generateRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Change user password với validation
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = findById(userId);

        // Validate current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng");
        }

        // Encode and save new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", userId);
    }

    /**
     * Update user avatar URL
     */
    public User updateAvatar(Long userId, String avatarUrl) {
        User user = findById(userId);
        user.setAvatarUrl(avatarUrl);
        return userRepository.save(user);
    }

    /**
     * Get user statistics for dashboard
     */
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();

        List<User> allUsers = userRepository.findAll();

        stats.put("totalUsers", allUsers.size());
        stats.put("activeUsers", allUsers.stream().filter(User::getIsActive).count());
        stats.put("studentsCount", allUsers.stream().filter(u -> u.getRole() == User.Role.STUDENT).count());
        stats.put("teachersCount", allUsers.stream().filter(u -> u.getRole() == User.Role.TEACHER).count());
        stats.put("adminsCount", allUsers.stream().filter(u -> u.getRole() == User.Role.ADMIN).count());

        return stats;
    }

    /**
     * Cập nhật profile user (không yêu cầu password/email)
     */
    public User updateProfile(Long userId, UserProfileUpdateDto updateDto) {
        log.info("Updating profile for user ID: {}", userId);

        User user = findById(userId);

        // Chỉ update các field profile, không touch password/email/role
        user.setFullName(updateDto.getFullName());
        user.setSchool(updateDto.getSchool());
        user.setMajor(updateDto.getMajor());
        user.setAcademicYear(updateDto.getAcademicYear());

        User savedUser = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", user.getEmail());

        return savedUser;
    }


}