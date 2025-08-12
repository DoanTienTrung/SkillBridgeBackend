package com.skillbridge.skillbridge_backend.Service;

import com.skillbridge.skillbridge_backend.dto.VocabularyCreateDto;
import com.skillbridge.skillbridge_backend.dto.LessonVocabularyDto;
import com.skillbridge.skillbridge_backend.entity.*;
import com.skillbridge.skillbridge_backend.repository.*;
import com.skillbridge.skillbridge_backend.exception.LessonNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class VocabularyService {

    private final LessonVocabularyRepository lessonVocabularyRepository;
    private final VocabularyRepository vocabularyRepository;
    private final ListeningLessonRepository lessonRepository;

    public VocabularyService(LessonVocabularyRepository lessonVocabularyRepository,
                             VocabularyRepository vocabularyRepository,
                             ListeningLessonRepository lessonRepository) {
        this.lessonVocabularyRepository = lessonVocabularyRepository;
        this.vocabularyRepository = vocabularyRepository;
        this.lessonRepository = lessonRepository;
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
}