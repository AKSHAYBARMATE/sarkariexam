package com.helixion.sarkariPariksha.Services;

import com.helixion.sarkariPariksha.Request.MockTestRequest;
import com.helixion.sarkariPariksha.Request.MockTestSubmissionRequest;
import com.helixion.sarkariPariksha.Response.MockTestResponse;
import com.helixion.sarkariPariksha.Response.MockTestSubmissionResponse;
import com.helixion.sarkariPariksha.Utils.BaseResponse;

import java.util.List;

public interface MockTestServices {
    BaseResponse<MockTestResponse> saveMockTest(MockTestRequest mockTestRequest);
    BaseResponse<MockTestResponse> saveMockTests(List<MockTestRequest> mockTestRequests);
    BaseResponse<MockTestResponse> getMockTestById(Long id);
    BaseResponse<MockTestResponse> getAllMockTests();
    BaseResponse<MockTestResponse> getMockTestsByExamId(Long examId);
    BaseResponse<MockTestResponse> getMockTestsByTestSeriesId(Long testSeriesId);
    BaseResponse<MockTestResponse> getMockTestsByType(String testType);
    BaseResponse<String> deleteMockTestById(Long id);

    // Mock test paper response save (batch submission via LoginUser context)
    BaseResponse<MockTestSubmissionResponse> submitMockTest(MockTestSubmissionRequest request);

    // Get attempt result by attempt ID
    BaseResponse<MockTestSubmissionResponse> getAttemptResult(Long attemptId);
}
