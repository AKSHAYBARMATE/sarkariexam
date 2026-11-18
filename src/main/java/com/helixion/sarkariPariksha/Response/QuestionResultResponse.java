package com.helixion.sarkariPariksha.Response;

import lombok.Data;

@Data
public class QuestionResultResponse {

    private Long questionId;
    private String questionText;
    private Long selectedOptionId;
    private String selectedOptionText;
    private Long correctOptionId;
    private String correctOptionText;
    private Boolean isCorrect;
    private Double marksAwarded;
    private Integer timeTakenSeconds;
    private String attemptStatus;
    private String explanation;
}
