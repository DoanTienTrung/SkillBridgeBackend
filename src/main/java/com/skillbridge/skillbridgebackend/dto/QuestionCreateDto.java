package com.skillbridge.skillbridgebackend.dto;

import com.skillbridge.skillbridgebackend.entity.Question;

public class QuestionCreateDto {
    private String questionText;
    private Question.QuestionType questionType;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    private String explanation;
    private Integer points;
    private Long lessonId;
    private Question.LessonType lessonType;

    // Constructors
    public QuestionCreateDto() {}

    public QuestionCreateDto(String questionText, Question.QuestionType questionType, String optionA, String optionB, String optionC, String optionD, String correctAnswer, String explanation, Integer points, Long lessonId, Question.LessonType lessonType) {
        this.questionText = questionText;
        this.questionType = questionType;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.points = points;
        this.lessonId = lessonId;
        this.lessonType = lessonType;
    }

    // Getters and Setters
    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public Question.QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(Question.QuestionType questionType) {
        this.questionType = questionType;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }

    public Question.LessonType getLessonType() {
        return lessonType;
    }

    public void setLessonType(Question.LessonType lessonType) {
        this.lessonType = lessonType;
    }


}
