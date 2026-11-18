package com.helixion.sarkariPariksha.Request;

import lombok.Data;

import java.util.List;

@Data
public class MockTestSubmissionRequest {

    // Konsa mock test submit kar rahe hain
    private Long mockTestId;

    // Total time in seconds
    private Integer totalTimeTakenSeconds;

    // Sabhi questions ka response ek sath (batch request)
    private List<QuestionAnswerRequest> answers;
}
