package com.skillbridge.skillbridgebackend.Service;

import com.skillbridge.skillbridgebackend.dto.VocabularyCreateDto;
import com.skillbridge.skillbridgebackend.dto.LessonVocabularyDto;
import com.skillbridge.skillbridgebackend.dto.PersonalVocabularyCreateDto;
import com.skillbridge.skillbridgebackend.entity.*;
import com.skillbridge.skillbridgebackend.repository.*;
import com.skillbridge.skillbridgebackend.exception.LessonNotFoundException;
import com.skillbridge.skillbridgebackend.exception.UserNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class VocabularyService {

    private final LessonVocabularyRepository lessonVocabularyRepository;
    private final VocabularyRepository vocabularyRepository;
    private final ListeningLessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final UserVocabularyRepository userVocabularyRepository;

    public VocabularyService(LessonVocabularyRepository lessonVocabularyRepository,
                             VocabularyRepository vocabularyRepository,
                             ListeningLessonRepository lessonRepository,
                             UserRepository userRepository,
                             UserVocabularyRepository userVocabularyRepository) {
        this.lessonVocabularyRepository = lessonVocabularyRepository;
        this.vocabularyRepository = vocabularyRepository;
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;
        this.userVocabularyRepository = userVocabularyRepository;
    }

    public LessonVocabularyDto addVocabularyToLesson(Long lessonId, VocabularyCreateDto createDto) {
        // Validate lesson exists
        ListeningLesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Không tìm thấy bài học"));

        // Validate position
        if (createDto.getEndPosition() <= createDto.getStartPosition()) {
            throw new IllegalArgumentException("Vị trí kết thúc phải lớn hơn vị trí bắt đầu");
        }

        // Check for overlapping vocabularies
        List<LessonVocabulary> overlapping = lessonVocabularyRepository
                .findOverlappingVocabularies(lessonId, createDto.getStartPosition(), createDto.getEndPosition());

        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("Vị trí đã có từ vựng khác, vui lòng chọn vị trí khác");
        }

        // Create or find vocabulary
        Vocabulary vocabulary = vocabularyRepository.findByWord(createDto.getWord())
                .orElseGet(() -> {
                    Vocabulary newVocab = new Vocabulary();
                    newVocab.setWord(createDto.getWord());
                    newVocab.setPhonetic(createDto.getPhonetic());
                    newVocab.setMeaning(createDto.getMeaning());
                    newVocab.setExampleSentence(createDto.getExampleSentence());
                    return vocabularyRepository.save(newVocab);
                });

        // Create lesson vocabulary
        LessonVocabulary lessonVocabulary = new LessonVocabulary();
        lessonVocabulary.setLesson(lesson);
        lessonVocabulary.setVocabulary(vocabulary);
        lessonVocabulary.setStartPosition(createDto.getStartPosition());
        lessonVocabulary.setEndPosition(createDto.getEndPosition());
        lessonVocabulary.setHighlightColor(createDto.getHighlightColor());

        LessonVocabulary saved = lessonVocabularyRepository.save(lessonVocabulary);

        return convertToDto(saved, lesson.getTranscript());
    }

    public List<LessonVocabularyDto> getLessonVocabularies(Long lessonId) {
        ListeningLesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Không tìm thấy bài học"));

        List<LessonVocabulary> vocabularies = lessonVocabularyRepository
                .findByLessonIdOrderByStartPosition(lessonId);

        return vocabularies.stream()
                .map(lv -> convertToDto(lv, lesson.getTranscript()))
                .collect(Collectors.toList());
    }

    public void removeVocabularyFromLesson(Long lessonId, Long vocabularyId) {
        LessonVocabulary lessonVocabulary = lessonVocabularyRepository
                .findByLessonIdAndVocabularyId(lessonId, vocabularyId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy từ vựng trong bài học"));

        lessonVocabularyRepository.delete(lessonVocabulary);
    }

    private LessonVocabularyDto convertToDto(LessonVocabulary lessonVocabulary, String transcript) {
        LessonVocabularyDto dto = new LessonVocabularyDto();
        dto.setId(lessonVocabulary.getId());
        dto.setWord(lessonVocabulary.getVocabulary().getWord());
        dto.setPhonetic(lessonVocabulary.getVocabulary().getPhonetic());
        dto.setMeaning(lessonVocabulary.getVocabulary().getMeaning());
        dto.setExampleSentence(lessonVocabulary.getVocabulary().getExampleSentence());
        dto.setStartPosition(lessonVocabulary.getStartPosition());
        dto.setEndPosition(lessonVocabulary.getEndPosition());
        dto.setHighlightColor(lessonVocabulary.getHighlightColor());

        // Extract selected text from transcript
        if (transcript != null &&
                lessonVocabulary.getStartPosition() < transcript.length() &&
                lessonVocabulary.getEndPosition() <= transcript.length()) {

            String selectedText = transcript.substring(
                    lessonVocabulary.getStartPosition(),
                    lessonVocabulary.getEndPosition()
            );
            dto.setSelectedText(selectedText);
        }

        return dto;
    }
    
    // ===== PERSONAL VOCABULARY MANAGEMENT =====
    
    /**
     * Save word to user's personal vocabulary
     */
    public UserVocabulary saveToPersonalVocabulary(Long userId, PersonalVocabularyCreateDto vocabularyDto) {
        log.info("Saving word '{}' to personal vocabulary for user: {}", vocabularyDto.getWord(), userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng"));
        
        // Check if already exists
        Optional<UserVocabulary> existing = userVocabularyRepository.findByUserIdAndWord(userId, vocabularyDto.getWord());
        if (existing.isPresent()) {
            throw new RuntimeException("Từ vựng đã có trong danh sách cá nhân");
        }
        
        // Find or create vocabulary
        Vocabulary vocabulary = vocabularyRepository.findByWord(vocabularyDto.getWord())
            .orElseGet(() -> {
                Vocabulary newVocab = new Vocabulary();
                newVocab.setWord(vocabularyDto.getWord());
                newVocab.setPhonetic(vocabularyDto.getPhonetic());
                newVocab.setMeaning(vocabularyDto.getMeaning());
                newVocab.setExampleSentence(vocabularyDto.getExampleSentence());
                newVocab.setCategory(vocabularyDto.getCategory());
                newVocab.setDifficulty(vocabularyDto.getDifficulty());
                newVocab.setPartOfSpeech(vocabularyDto.getPartOfSpeech());
                newVocab.setSynonyms(vocabularyDto.getSynonyms());
                newVocab.setAntonyms(vocabularyDto.getAntonyms());
                newVocab.setNotes(vocabularyDto.getNotes());
                return vocabularyRepository.save(newVocab);
            });
        
        // Create user vocabulary
        UserVocabulary userVocabulary = new UserVocabulary();
        userVocabulary.setUser(user);
        userVocabulary.setVocabulary(vocabulary);
        userVocabulary.setStatus(UserVocabulary.Status.LEARNING);
        
        return userVocabularyRepository.save(userVocabulary);
    }
    
    /**
     * Get user's personal vocabulary list
     */
    public List<UserVocabulary> getUserVocabulary(Long userId) {
        log.info("Getting personal vocabulary for user: {}", userId);
        return userVocabularyRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Update vocabulary status (learning, mastered, difficult)
     */
    public UserVocabulary updateVocabularyStatus(Long userId, Long vocabularyId, UserVocabulary.Status status) {
        UserVocabulary userVocab = userVocabularyRepository.findByUserIdAndVocabularyId(userId, vocabularyId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy từ vựng trong danh sách cá nhân"));
        
        userVocab.setStatus(status);
        return userVocabularyRepository.save(userVocab);
    }
    
    /**
     * Remove word from personal vocabulary
     */
    public void removeFromPersonalVocabulary(Long userId, Long vocabularyId) {
        UserVocabulary userVocab = userVocabularyRepository.findByUserIdAndVocabularyId(userId, vocabularyId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy từ vựng trong danh sách cá nhân"));
        
        userVocabularyRepository.delete(userVocab);
    }
    
    /**
     * Look up word meaning (dictionary feature)
     */
    public Vocabulary lookupWord(String word) {
        return vocabularyRepository.findByWord(word.toLowerCase())
            .orElse(null); // Return null if not found, frontend will handle external dictionary API
    }
}