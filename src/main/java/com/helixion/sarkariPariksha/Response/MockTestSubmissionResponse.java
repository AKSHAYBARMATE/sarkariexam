package com.helixion.sarkariPariksha.Response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MockTestSubmissionResponse {

    private Long attemptId;
    private Long mockTestId;
    private String mockTestName;
    private Long candidateId;
    private String candidateName;
    private Integer attemptNumber;

    private Integer totalQuestions;
    private Integer totalAttempted;
    private Integer totalCorrect;
    private Integer totalIncorrect;
    private Integer totalSkipped;

    private Double score;
    private Double totalMarks;
    private Double percentage;
    private Double accuracy;
    private Integer totalTimeTakenSeconds;

    private String status;
    private LocalDateTime submitTime;

    private List<QuestionResultResponse> questionResults;
}
