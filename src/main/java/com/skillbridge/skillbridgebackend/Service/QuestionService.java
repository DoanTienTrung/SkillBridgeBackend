package com.skillbridge.skillbridgebackend.Service;

import com.skillbridge.skillbridgebackend.dto.QuestionCreateDto;
import com.skillbridge.skillbridgebackend.dto.QuestionDto;
import com.skillbridge.skillbridgebackend.dto.QuestionUpdateDto;
import com.skillbridge.skillbridgebackend.entity.ListeningLesson;
import com.skillbridge.skillbridgebackend.entity.Question;
import com.skillbridge.skillbridgebackend.entity.ReadingLesson;
import com.skillbridge.skillbridgebackend.exception.LessonNotFoundException;
import com.skillbridge.skillbridgebackend.mapper.QuestionMapper;
import com.skillbridge.skillbridgebackend.repository.ListeningLessonRepository;
import com.skillbridge.skillbridgebackend.repository.QuestionRepository;
import com.skillbridge.skillbridgebackend.repository.ReadingLessonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuestionService {
    
    private final QuestionRepository questionRepository;
    private final ListeningLessonRepository listeningLessonRepository;
    private final ReadingLessonRepository readingLessonRepository;
    private final QuestionMapper questionMapper;
    
    // Constructor injection
    public QuestionService(QuestionRepository questionRepository,
                          ListeningLessonRepository listeningLessonRepository,
                          ReadingLessonRepository readingLessonRepository,
                          QuestionMapper questionMapper) {
        this.questionRepository = questionRepository;
        this.listeningLessonRepository = listeningLessonRepository;
        this.readingLessonRepository = readingLessonRepository;
        this.questionMapper = questionMapper;
    }
    
    public List<QuestionDto> getQuestionsByLesson(Long lessonId, Question.LessonType lessonType) {
        List<Question> questions = questionRepository.findByLessonIdAndType(lessonId);
        return questions.stream()
                .map(questionMapper::toDto)
                .collect(Collectors.toList());
    }

    public QuestionDto createQuestion(QuestionCreateDto dto) {
        Question question = questionMapper.toEntity(dto);

        // Set lesson relationship với logic rõ ràng
        if (dto.getLessonType() == Question.LessonType.LISTENING) {
            ListeningLesson lesson = listeningLessonRepository.findById(dto.getLessonId())
                    .orElseThrow(() -> new LessonNotFoundException(
                            String.format("Listening lesson with ID %d not found", dto.getLessonId())
                    ));
            question.setListeningLesson(lesson);
            question.setReadingLesson(null); // ⚠️ QUAN TRỌNG: Set null cho relationship không sử dụng
        } else if (dto.getLessonType() == Question.LessonType.READING) {
            ReadingLesson lesson = readingLessonRepository.findById(dto.getLessonId())
                    .orElseThrow(() -> new LessonNotFoundException(
                            String.format("Reading lesson with ID %d not found", dto.getLessonId())
                    ));
            question.setReadingLesson(lesson);
            question.setListeningLesson(null); // ⚠️ QUAN TRỌNG: Set null cho relationship không sử dụng
        } else {
            throw new IllegalArgumentException("Invalid lesson type: " + dto.getLessonType());
        }

        Question savedQuestion = questionRepository.save(question);
        return questionMapper.toDto(savedQuestion);
    }
    
    public QuestionDto updateQuestion(Long id, QuestionUpdateDto dto) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        
        // Update fields
        question.setQuestionText(dto.getQuestionText());
        question.setQuestionType(dto.getQuestionType());
        question.setOptionA(dto.getOptionA());
        question.setOptionB(dto.getOptionB());
        question.setOptionC(dto.getOptionC());
        question.setOptionD(dto.getOptionD());
        question.setCorrectAnswer(dto.getCorrectAnswer());
        question.setExplanation(dto.getExplanation());
        question.setPoints(dto.getPoints());
        
        Question updatedQuestion = questionRepository.save(question);
        return questionMapper.toDto(updatedQuestion);
    }
    
    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new RuntimeException("Question not found");
        }
        questionRepository.deleteById(id);
    }
    
    public QuestionDto getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        return questionMapper.toDto(question);
    }
}