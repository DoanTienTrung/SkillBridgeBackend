package com.skillbridge.skillbridgebackend.Service;

import com.skillbridge.skillbridgebackend.dto.analytics.*;
import com.skillbridge.skillbridgebackend.entity.*;
import com.skillbridge.skillbridgebackend.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class AnalyticsService {

    @Autowired
    private UserRepository userRepository;

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

    @Autowired
    private VocabularyRepository vocabularyRepository;

    /**
     * Lấy analytics tổng quan của hệ thống
     */
    public SystemAnalyticsDto getSystemAnalytics() {
        log.info("Getting system analytics");

        SystemAnalyticsDto analytics = new SystemAnalyticsDto();

        // Tổng số users
        analytics.setTotalUsers(userRepository.countByIsActiveTrue());
        analytics.setTotalStudents(userRepository.countByRoleAndIsActiveTrue(User.Role.STUDENT));
        analytics.setTotalTeachers(userRepository.countByRoleAndIsActiveTrue(User.Role.TEACHER));

        // Tổng số lessons
        analytics.setTotalListeningLessons(listeningLessonRepository.countByStatus(ListeningLesson.Status.PUBLISHED));
        analytics.setTotalReadingLessons(readingLessonRepository.countByStatus(ListeningLesson.Status.PUBLISHED));

        // Tổng số questions và vocabulary
        analytics.setTotalQuestions((int) questionRepository.count());
        analytics.setTotalVocabulary((int) vocabularyRepository.count());

        // Hoạt động hôm nay
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);

        // Users hoạt động hôm nay (có progress hoặc answers hôm nay)
        List<Long> activeUserIds = progressRepository.findUserIdsWithActivityBetween(todayStart, todayEnd);
        analytics.setActiveUsersToday(activeUserIds.size());

        // Đăng ký mới tuần này
        LocalDateTime weekStart = LocalDate.now().minusDays(6).atStartOfDay();
        analytics.setNewRegistrationsThisWeek(userRepository.countByCreatedAtBetween(weekStart, LocalDateTime.now()));

        // Lessons hoàn thành hôm nay
        analytics.setCompletedLessonsToday(progressRepository.countByCompletedAtBetween(todayStart, todayEnd));

        // Hoạt động 7 ngày qua
        analytics.setWeeklyActivity(getWeeklyActivity());

        return analytics;
    }

    /**
     * Lấy hoạt động 7 ngày qua
     */
    public List<DailyActivityDto> getWeeklyActivity() {
        List<DailyActivityDto> weeklyActivity = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(23, 59, 59);

            DailyActivityDto activity = new DailyActivityDto();
            activity.setDate(date);
            activity.setDayName(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("vi-VN")));

            // Active users
            List<Long> activeUsers = progressRepository.findUserIdsWithActivityBetween(dayStart, dayEnd);
            activity.setActiveUsers(activeUsers.size());

            // Completed lessons
            activity.setCompletedLessons(progressRepository.countByCompletedAtBetween(dayStart, dayEnd));

            // New registrations
            activity.setNewRegistrations(userRepository.countByCreatedAtBetween(dayStart, dayEnd));

            // Average score
            Double avgScore = progressRepository.findAverageScoreByCompletedAtBetween(dayStart, dayEnd);
            activity.setAverageScore(avgScore != null ? 
                BigDecimal.valueOf(avgScore).setScale(2, RoundingMode.HALF_UP).doubleValue() : 0.0);

            weeklyActivity.add(activity);
        }

        return weeklyActivity;
    }

    /**
     * Lấy analytics cho tất cả lessons
     */
    public List<LessonAnalyticsDto> getAllLessonsAnalytics() {
        log.info("Getting analytics for all lessons");

        List<LessonAnalyticsDto> result = new ArrayList<>();

        // Analytics cho Listening Lessons
        List<ListeningLesson> listeningLessons = listeningLessonRepository.findByStatus(ListeningLesson.Status.PUBLISHED);
        for (ListeningLesson lesson : listeningLessons) {
            result.add(getListeningLessonAnalytics(lesson));
        }

        // Analytics cho Reading Lessons
        List<ReadingLesson> readingLessons = readingLessonRepository.findByStatus(ListeningLesson.Status.PUBLISHED);
        for (ReadingLesson lesson : readingLessons) {
            result.add(getReadingLessonAnalytics(lesson));
        }

        // Sort by completion rate descending
        result.sort((a, b) -> Double.compare(b.getCompletionRate(), a.getCompletionRate()));

        return result;
    }

    /**
     * Lấy analytics cho một listening lesson
     */
    public LessonAnalyticsDto getLessonAnalytics(Long lessonId, String lessonType) {
        if ("LISTENING".equals(lessonType)) {
            ListeningLesson lesson = listeningLessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Listening lesson not found"));
            return getListeningLessonAnalytics(lesson);
        } else if ("READING".equals(lessonType)) {
            ReadingLesson lesson = readingLessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Reading lesson not found"));
            return getReadingLessonAnalytics(lesson);
        } else {
            throw new IllegalArgumentException("Invalid lesson type: " + lessonType);
        }
    }

    private LessonAnalyticsDto getListeningLessonAnalytics(ListeningLesson lesson) {
        LessonAnalyticsDto analytics = new LessonAnalyticsDto();
        analytics.setLessonId(lesson.getId());
        analytics.setLessonTitle(lesson.getTitle());
        analytics.setLessonType("LISTENING");
        analytics.setLevel(lesson.getLevel().name());
        analytics.setCategoryName(lesson.getCategory() != null ? lesson.getCategory().getName() : "N/A");

        // Progress data
        List<UserLessonProgress> progressList = progressRepository
            .findByLessonIdAndLessonType(lesson.getId(), Question.LessonType.LISTENING);

        analytics.setTotalViews(progressList.size());
        analytics.setCompletedCount((int) progressList.stream().filter(UserLessonProgress::getIsCompleted).count());
        analytics.setCompletionRate(progressList.size() > 0 ? 
            (double) analytics.getCompletedCount() / progressList.size() * 100 : 0.0);

        // Average score và time
        Double avgScore = progressList.stream()
            .filter(p -> p.getScore() != null)
            .mapToDouble(p -> p.getScore().doubleValue())
            .average().orElse(0.0);
        analytics.setAverageScore(BigDecimal.valueOf(avgScore).setScale(2, RoundingMode.HALF_UP).doubleValue());

        Double avgTime = progressList.stream()
            .filter(p -> p.getTimeSpentSeconds() != null)
            .mapToInt(UserLessonProgress::getTimeSpentSeconds)
            .average().orElse(0.0);
        analytics.setAverageTimeSpent((int) Math.round(avgTime));

        // Questions analytics
        List<Question> questions = questionRepository.findByListeningLessonId(lesson.getId());
        analytics.setTotalQuestions(questions.size());
        analytics.setQuestionStats(getQuestionAnalytics(questions));

        return analytics;
    }

    private LessonAnalyticsDto getReadingLessonAnalytics(ReadingLesson lesson) {
        LessonAnalyticsDto analytics = new LessonAnalyticsDto();
        analytics.setLessonId(lesson.getId());
        analytics.setLessonTitle(lesson.getTitle());
        analytics.setLessonType("READING");
        analytics.setLevel(lesson.getLevel().name());
        analytics.setCategoryName(lesson.getCategory() != null ? lesson.getCategory().getName() : "N/A");

        // Progress data
        List<UserLessonProgress> progressList = progressRepository
            .findByLessonIdAndLessonType(lesson.getId(), Question.LessonType.READING);

        analytics.setTotalViews(progressList.size());
        analytics.setCompletedCount((int) progressList.stream().filter(UserLessonProgress::getIsCompleted).count());
        analytics.setCompletionRate(progressList.size() > 0 ? 
            (double) analytics.getCompletedCount() / progressList.size() * 100 : 0.0);

        // Average score và time
        Double avgScore = progressList.stream()
            .filter(p -> p.getScore() != null)
            .mapToDouble(p -> p.getScore().doubleValue())
            .average().orElse(0.0);
        analytics.setAverageScore(BigDecimal.valueOf(avgScore).setScale(2, RoundingMode.HALF_UP).doubleValue());

        Double avgTime = progressList.stream()
            .filter(p -> p.getTimeSpentSeconds() != null)
            .mapToInt(UserLessonProgress::getTimeSpentSeconds)
            .average().orElse(0.0);
        analytics.setAverageTimeSpent((int) Math.round(avgTime));

        // Questions analytics
        List<Question> questions = questionRepository.findByReadingLessonId(lesson.getId());
        analytics.setTotalQuestions(questions.size());
        analytics.setQuestionStats(getQuestionAnalytics(questions));

        return analytics;
    }

    private List<QuestionAnalyticsDto> getQuestionAnalytics(List<Question> questions) {
        return questions.stream().map(this::getQuestionAnalytics).collect(Collectors.toList());
    }

    private QuestionAnalyticsDto getQuestionAnalytics(Question question) {
        QuestionAnalyticsDto analytics = new QuestionAnalyticsDto();
        analytics.setQuestionId(question.getId());
        analytics.setQuestionText(question.getQuestionText());
        analytics.setQuestionType(question.getQuestionType().name());
        analytics.setCorrectAnswer(question.getCorrectAnswer());

        List<UserAnswer> answers = userAnswerRepository.findByQuestionId(question.getId());
        analytics.setTotalAnswers(answers.size());
        analytics.setCorrectAnswers((int) answers.stream().filter(UserAnswer::getIsCorrect).count());
        analytics.setAccuracyRate(answers.size() > 0 ? 
            (double) analytics.getCorrectAnswers() / answers.size() * 100 : 0.0);

        // Most selected wrong answer
        Map<String, Long> wrongAnswers = answers.stream()
            .filter(a -> !a.getIsCorrect())
            .collect(Collectors.groupingBy(UserAnswer::getSelectedAnswer, Collectors.counting()));
        
        String mostWrong = wrongAnswers.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
        analytics.setMostSelectedWrongAnswer(mostWrong);

        return analytics;
    }

    /**
     * Lấy báo cáo chi tiết của một học viên
     */
    public StudentProgressReportDto getStudentReport(Long studentId) {
        log.info("Getting progress report for student: {}", studentId);

        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!User.Role.STUDENT.equals(student.getRole())) {
            throw new RuntimeException("User is not a student");
        }

        StudentProgressReportDto report = new StudentProgressReportDto();
        report.setStudentId(student.getId());
        report.setStudentName(student.getFullName());
        report.setStudentEmail(student.getEmail());
        report.setSchool(student.getSchool());
        report.setMajor(student.getMajor());
        report.setAcademicYear(student.getAcademicYear());
        report.setRegistrationDate(student.getCreatedAt());

        // Progress data
        List<UserLessonProgress> allProgress = progressRepository.findByUserId(studentId);
        report.setTotalLessonsCompleted((int) allProgress.stream().filter(UserLessonProgress::getIsCompleted).count());

        long listeningCompleted = allProgress.stream()
            .filter(p -> Question.LessonType.LISTENING.equals(p.getLessonType()) && p.getIsCompleted())
            .count();
        report.setListeningLessonsCompleted((int) listeningCompleted);

        long readingCompleted = allProgress.stream()
            .filter(p -> Question.LessonType.READING.equals(p.getLessonType()) && p.getIsCompleted())
            .count();
        report.setReadingLessonsCompleted((int) readingCompleted);

        // Average score
        Double avgScore = allProgress.stream()
            .filter(p -> p.getScore() != null)
            .mapToDouble(p -> p.getScore().doubleValue())
            .average().orElse(0.0);
        report.setAverageScore(BigDecimal.valueOf(avgScore).setScale(2, RoundingMode.HALF_UP).doubleValue());

        // Total time studied
        int totalTime = allProgress.stream()
            .filter(p -> p.getTimeSpentSeconds() != null)
            .mapToInt(UserLessonProgress::getTimeSpentSeconds)
            .sum();
        report.setTotalTimeStudiedSeconds(totalTime);

        // Vocabulary learned
        int vocabLearned = userVocabularyRepository.countByUserIdAndIsLearnedTrue(studentId);
        report.setVocabularyLearned(vocabLearned);

        // Last activity
        LocalDateTime lastActivity = allProgress.stream()
            .map(UserLessonProgress::getCreatedAt)
            .max(LocalDateTime::compareTo)
            .orElse(student.getCreatedAt());
        report.setLastActivity(lastActivity);

        // Lesson details
        List<LessonProgressDetailDto> lessonDetails = new ArrayList<>();
        for (UserLessonProgress progress : allProgress) {
            lessonDetails.add(getLessonProgressDetail(progress));
        }
        report.setLessonDetails(lessonDetails);

        return report;
    }

    private LessonProgressDetailDto getLessonProgressDetail(UserLessonProgress progress) {
        LessonProgressDetailDto detail = new LessonProgressDetailDto();
        detail.setLessonId(progress.getLessonId());
        detail.setLessonType(progress.getLessonType().name());
        detail.setIsCompleted(progress.getIsCompleted());
        detail.setScore(progress.getScore());
        detail.setTimeSpentSeconds(progress.getTimeSpentSeconds());
        detail.setCompletedAt(progress.getCompletedAt());

        // Get lesson title and other info
        if (Question.LessonType.LISTENING.equals(progress.getLessonType())) {
            ListeningLesson lesson = listeningLessonRepository.findById(progress.getLessonId()).orElse(null);
            if (lesson != null) {
                detail.setLessonTitle(lesson.getTitle());
                detail.setLevel(lesson.getLevel().name());
                detail.setCategoryName(lesson.getCategory() != null ? lesson.getCategory().getName() : "N/A");
                
                // Questions stats
                List<Question> questions = questionRepository.findByListeningLessonId(lesson.getId());
                detail.setTotalQuestions(questions.size());
                
                List<UserAnswer> userAnswers = userAnswerRepository.findByUserIdAndQuestionIn(
                    progress.getUser().getId(), questions);
                detail.setCorrectAnswers((int) userAnswers.stream().filter(UserAnswer::getIsCorrect).count());
            }
        } else {
            ReadingLesson lesson = readingLessonRepository.findById(progress.getLessonId()).orElse(null);
            if (lesson != null) {
                detail.setLessonTitle(lesson.getTitle());
                detail.setLevel(lesson.getLevel().name());
                detail.setCategoryName(lesson.getCategory() != null ? lesson.getCategory().getName() : "N/A");
                
                // Questions stats
                List<Question> questions = questionRepository.findByReadingLessonId(lesson.getId());
                detail.setTotalQuestions(questions.size());
                
                List<UserAnswer> userAnswers = userAnswerRepository.findByUserIdAndQuestionIn(
                    progress.getUser().getId(), questions);
                detail.setCorrectAnswers((int) userAnswers.stream().filter(UserAnswer::getIsCorrect).count());
            }
        }

        return detail;
    }

    /**
     * Lấy báo cáo tất cả học viên
     */
    public List<StudentProgressReportDto> getAllStudentsReports() {
        log.info("Getting progress reports for all students");

        List<User> students = userRepository.findByRoleAndIsActiveTrue(User.Role.STUDENT);
        return students.stream()
            .map(student -> getStudentReport(student.getId()))
            .collect(Collectors.toList());
    }
}
