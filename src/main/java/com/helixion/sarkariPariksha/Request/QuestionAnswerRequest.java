package com.helixion.sarkariPariksha.Request;

import lombok.Data;

@Data
public class QuestionAnswerRequest {

    private Long questionId;

    // Selected option ID (null agar question skip kiya)
    private Long selectedOptionId;

    // Is question par kitne seconds lagaye
    private Integer timeTakenSeconds = 0;

    // Review ke liye mark kiya ya nahi
    private Boolean isMarkedForReview = false;

    // ANSWERED, NOT_ANSWERED, MARKED_FOR_REVIEW, SKIPPED
    private String attemptStatus;
}
