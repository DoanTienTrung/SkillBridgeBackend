package com.skillbridge.skillbridgebackend.mapper;

import com.skillbridge.skillbridgebackend.dto.QuestionCreateDto;
import com.skillbridge.skillbridgebackend.dto.QuestionDto;
import com.skillbridge.skillbridgebackend.entity.Question;
import org.springframework.stereotype.Component;

@Component
public class QuestionMapper {

    public QuestionDto toDto(Question question) {
        QuestionDto dto = new QuestionDto();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionType(question.getQuestionType().name());
        dto.setOptionA(question.getOptionA());
        dto.setOptionB(question.getOptionB());
        dto.setOptionC(question.getOptionC());
        dto.setOptionD(question.getOptionD());
        dto.setCorrectAnswer(question.getCorrectAnswer());
        dto.setExplanation(question.getExplanation());
        dto.setPoints(question.getPoints());

        // Set lesson info
        if (question.getListeningLesson() != null) {
            dto.setLessonId(question.getListeningLesson().getId());
            dto.setLessonType("LISTENING");
        } else if (question.getReadingLesson() != null) {
            dto.setLessonId(question.getReadingLesson().getId());
            dto.setLessonType("READING");
        }

        return dto;
    }

    public Question toEntity(QuestionCreateDto dto) {
        Question question = new Question();
        question.setQuestionText(dto.getQuestionText());
        question.setQuestionType(dto.getQuestionType());
        question.setOptionA(dto.getOptionA());
        question.setOptionB(dto.getOptionB());
        question.setOptionC(dto.getOptionC());
        question.setOptionD(dto.getOptionD());
        question.setCorrectAnswer(dto.getCorrectAnswer());
        question.setExplanation(dto.getExplanation());
        question.setPoints(dto.getPoints() != null ? dto.getPoints() : 1);
        question.setLessonType(dto.getLessonType());

        return question;
    }
}
