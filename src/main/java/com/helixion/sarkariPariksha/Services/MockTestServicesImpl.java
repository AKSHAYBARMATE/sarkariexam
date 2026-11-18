package com.helixion.sarkariPariksha.Services;

import com.helixion.sarkariPariksha.Entity.*;
import com.helixion.sarkariPariksha.Repository.*;
import com.helixion.sarkariPariksha.Request.MockTestRequest;
import com.helixion.sarkariPariksha.Request.MockTestSubmissionRequest;
import com.helixion.sarkariPariksha.Request.QuestionAnswerRequest;
import com.helixion.sarkariPariksha.Response.MockTestResponse;
import com.helixion.sarkariPariksha.Response.MockTestSubmissionResponse;
import com.helixion.sarkariPariksha.Response.QuestionResultResponse;
import com.helixion.sarkariPariksha.Security.CustomUserDetails;
import com.helixion.sarkariPariksha.Security.LoginUser;
import com.helixion.sarkariPariksha.Security.LoginUserContext;
import com.helixion.sarkariPariksha.Utils.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MockTestServicesImpl implements MockTestServices {

    @Autowired
    private MockTestRepository mockTestRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private TestSeriesRepository testSeriesRepository;

    @Autowired
    private MockTestAttemptRepository mockTestAttemptRepository;

    @Autowired
    private CandidatesRepository candidatesRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionOptionRepository questionOptionRepository;

    @Autowired
    private MockTestQuestionAttemptRepository mockTestQuestionAttemptRepository;

    @Override
    public BaseResponse<MockTestResponse> saveMockTest(MockTestRequest mockTestRequest) {
        if (mockTestRequest == null) {
            BaseResponse<MockTestResponse> baseResponse = new BaseResponse<>();
            baseResponse.setStatus(0);
            baseResponse.setCode(400);
            baseResponse.setMessage("Mock test request cannot be null");
            return baseResponse;
        }
        return saveMockTests(Collections.singletonList(mockTestRequest));
    }

    @Override
    @Transactional
    public BaseResponse<MockTestResponse> saveMockTests(List<MockTestRequest> mockTestRequests) {
        BaseResponse<MockTestResponse> baseResponse = new BaseResponse<>();
        try {
            if (mockTestRequests == null || mockTestRequests.isEmpty()) {
                baseResponse.setStatus(0);
                baseResponse.setCode(400);
                baseResponse.setMessage("Mock test request list cannot be empty");
                return baseResponse;
            }

            List<MockTestResponse> responseList = new ArrayList<>();

            for (MockTestRequest mockTestRequest : mockTestRequests) {
                if (mockTestRequest.getTestName() == null || mockTestRequest.getTestName().trim().isEmpty()) {
                    baseResponse.setStatus(0);
                    baseResponse.setCode(400);
                    baseResponse.setMessage("Test name is required");
                    return baseResponse;
                }

                if (mockTestRequest.getTestType() == null || mockTestRequest.getTestType().trim().isEmpty()) {
                    baseResponse.setStatus(0);
                    baseResponse.setCode(400);
                    baseResponse.setMessage("Test type is required (e.g. FULL_LENGTH, SECTIONAL, TOPIC_WISE)");
                    return baseResponse;
                }

                if (mockTestRequest.getExamId() == null) {
                    baseResponse.setStatus(0);
                    baseResponse.setCode(400);
                    baseResponse.setMessage("Exam ID is required for test: " + mockTestRequest.getTestName());
                    return baseResponse;
                }

                // Verify Exam exists
                Optional<ExamMaster> examOptional = examRepository.findById(mockTestRequest.getExamId());
                if (examOptional.isEmpty()) {
                    baseResponse.setStatus(0);
                    baseResponse.setCode(404);
                    baseResponse.setMessage("Exam not found with ID: " + mockTestRequest.getExamId());
                    return baseResponse;
                }

                MockTestMaster mockTestMaster = null;

                // Check if update by ID
                if (mockTestRequest.getId() != null) {
                    mockTestMaster = mockTestRepository.findById(mockTestRequest.getId()).orElse(null);
                    if (mockTestMaster == null) {
                        baseResponse.setStatus(0);
                        baseResponse.setCode(404);
                        baseResponse.setMessage("Mock test not found with ID: " + mockTestRequest.getId());
                        return baseResponse;
                    }
                } else {
                    mockTestMaster = new MockTestMaster();
                }

                // Map fields
                mockTestMaster.setExam(examOptional.get());

                // Assign TestSeries if provided
                if (mockTestRequest.getTestSeriesId() != null) {
                    Optional<TestSeries> tsOpt = testSeriesRepository.findById(mockTestRequest.getTestSeriesId());
                    tsOpt.ifPresent(mockTestMaster::setTestSeries);
                }

                mockTestMaster.setTestName(mockTestRequest.getTestName().trim());
                mockTestMaster.setTestType(mockTestRequest.getTestType().trim().toUpperCase());
                mockTestMaster.setTotalQuestions(mockTestRequest.getTotalQuestions());
                mockTestMaster.setTotalMarks(mockTestRequest.getTotalMarks());
                mockTestMaster.setDurationMinutes(mockTestRequest.getDurationMinutes());
                mockTestMaster.setNegativeMark(mockTestRequest.getNegativeMark());
                mockTestMaster.setInstructions(mockTestRequest.getInstructions());
                mockTestMaster.setFree(mockTestRequest.getFree() != null ? mockTestRequest.getFree() : false);
                mockTestMaster.setCreatedBy(mockTestRequest.getCreatedBy());
                mockTestMaster.setActive(mockTestRequest.getActive() != null ? mockTestRequest.getActive() : true);

                MockTestMaster savedMockTest = mockTestRepository.save(mockTestMaster);
                responseList.add(mapToMockTestResponse(savedMockTest));
            }

            baseResponse.setStatus(1);
            baseResponse.setCode(200);
            baseResponse.setMessage(responseList.size() + " mock test(s) saved successfully");
            baseResponse.setData(responseList);

        } catch (Exception exception) {
            log.error("Exception in saveMockTests: ", exception);
            baseResponse.setStatus(0);
            baseResponse.setCode(500);
            baseResponse.setMessage("Failed to save mock tests: " + exception.getMessage());
            baseResponse.setData(null);
        }
        return baseResponse;
    }

    @Override
    public BaseResponse<MockTestResponse> getMockTestById(Long id) {
        BaseResponse<MockTestResponse> baseResponse = new BaseResponse<>();
        try {
            if (id == null) {
                baseResponse.setStatus(0);
                baseResponse.setCode(400);
                baseResponse.setMessage("Mock test ID cannot be null");
                return baseResponse;
            }

            Optional<MockTestMaster> mockTestOptional = mockTestRepository.findById(id);
            if (mockTestOptional.isEmpty()) {
                baseResponse.setStatus(0);
                baseResponse.setCode(404);
                baseResponse.setMessage("Mock test not found with ID: " + id);
                return baseResponse;
            }

            MockTestResponse response = mapToMockTestResponse(mockTestOptional.get());

            baseResponse.setStatus(1);
            baseResponse.setCode(200);
            baseResponse.setMessage("Mock test fetched successfully");
            baseResponse.setData(Collections.singletonList(response));

        } catch (Exception exception) {
            log.error("Exception in getMockTestById: ", exception);
            baseResponse.setStatus(0);
            baseResponse.setCode(500);
            baseResponse.setMessage("Failed to fetch mock test: " + exception.getMessage());
            baseResponse.setData(null);
        }
        return baseResponse;
    }

    @Override
    public BaseResponse<MockTestResponse> getAllMockTests() {
        BaseResponse<MockTestResponse> baseResponse = new BaseResponse<>();
        try {
            List<MockTestMaster> mockTests = mockTestRepository.findByActiveTrue();
            List<MockTestResponse> responseList = new ArrayList<>();
            for (MockTestMaster test : mockTests) {
                responseList.add(mapToMockTestResponse(test));
            }

            baseResponse.setStatus(1);
            baseResponse.setCode(200);
            baseResponse.setMessage("All active mock tests fetched successfully");
            baseResponse.setData(responseList);

        } catch (Exception exception) {
            log.error("Exception in getAllMockTests: ", exception);
            baseResponse.setStatus(0);
            baseResponse.setCode(500);
            baseResponse.setMessage("Failed to fetch mock tests: " + exception.getMessage());
            baseResponse.setData(null);
        }
        return baseResponse;
    }

    @Override
    public BaseResponse<MockTestResponse> getMockTestsByExamId(Long examId) {
        BaseResponse<MockTestResponse> baseResponse = new BaseResponse<>();
        try {
            if (examId == null) {
                baseResponse.setStatus(0);
                baseResponse.setCode(400);
                baseResponse.setMessage("Exam ID cannot be null");
                return baseResponse;
            }

            List<MockTestMaster> mockTests = mockTestRepository.findByExam_IdAndActiveTrue(examId);
            List<MockTestResponse> responseList = new ArrayList<>();
            for (MockTestMaster test : mockTests) {
                responseList.add(mapToMockTestResponse(test));
            }

            baseResponse.setStatus(1);
            baseResponse.setCode(200);
            baseResponse.setMessage("Mock tests for Exam ID " + examId + " fetched successfully");
            baseResponse.setData(responseList);

        } catch (Exception exception) {
            log.error("Exception in getMockTestsByExamId: ", exception);
            baseResponse.setStatus(0);
            baseResponse.setCode(500);
            baseResponse.setMessage("Failed to fetch mock tests by exam: " + exception.getMessage());
            baseResponse.setData(null);
        }
        return baseResponse;
    }

    @Override
    public BaseResponse<MockTestResponse> getMockTestsByTestSeriesId(Long testSeriesId) {
        BaseResponse<MockTestResponse> baseResponse = new BaseResponse<>();
        try {
            if (testSeriesId == null) {
                baseResponse.setStatus(0);
                baseResponse.setCode(400);
                baseResponse.setMessage("Test Series ID cannot be null");
                return baseResponse;
            }

            List<MockTestMaster> mockTests = mockTestRepository.findByTestSeries_IdAndActiveTrue(testSeriesId);
            List<MockTestResponse> responseList = new ArrayList<>();
            for (MockTestMaster test : mockTests) {
                responseList.add(mapToMockTestResponse(test));
            }

            baseResponse.setStatus(1);
            baseResponse.setCode(200);
            baseResponse.setMessage("Mock tests for Test Series ID " + testSeriesId + " fetched successfully");
            baseResponse.setData(responseList);

        } catch (Exception exception) {
            log.error("Exception in getMockTestsByTestSeriesId: ", exception);
            baseResponse.setStatus(0);
            baseResponse.setCode(500);
            baseResponse.setMessage("Failed to fetch mock tests by test series: " + exception.getMessage());
            baseResponse.setData(null);
        }
        return baseResponse;
    }

    @Override
    public BaseResponse<MockTestResponse> getMockTestsByType(String testType) {
        BaseResponse<MockTestResponse> baseResponse = new BaseResponse<>();
        try {
            if (testType == null || testType.trim().isEmpty()) {
                baseResponse.setStatus(0);
                baseResponse.setCode(400);
                baseResponse.setMessage("Test type cannot be null or empty");
                return baseResponse;
            }

            List<MockTestMaster> mockTests = mockTestRepository.findByTestTypeAndActiveTrue(testType.trim().toUpperCase());
            List<MockTestResponse> responseList = new ArrayList<>();
            for (MockTestMaster test : mockTests) {
                responseList.add(mapToMockTestResponse(test));
            }

            baseResponse.setStatus(1);
            baseResponse.setCode(200);
            baseResponse.setMessage("Mock tests of type '" + testType + "' fetched successfully");
            baseResponse.setData(responseList);

        } catch (Exception exception) {
            log.error("Exception in getMockTestsByType: ", exception);
            baseResponse.setStatus(0);
            baseResponse.setCode(500);
            baseResponse.setMessage("Failed to fetch mock tests by type: " + exception.getMessage());
            baseResponse.setData(null);
        }
        return baseResponse;
    }

    @Override
    public BaseResponse<String> deleteMockTestById(Long id) {
        BaseResponse<String> baseResponse = new BaseResponse<>();
        try {
            if (id == null) {
                baseResponse.setStatus(0);
                baseResponse.setCode(400);
                baseResponse.setMessage("Mock test ID cannot be null");
                return baseResponse;
            }

            Optional<MockTestMaster> mockTestOptional = mockTestRepository.findById(id);
            if (mockTestOptional.isEmpty()) {
                baseResponse.setStatus(0);
                baseResponse.setCode(404);
                baseResponse.setMessage("Mock test not found with ID: " + id);
                return baseResponse;
            }

            // Soft delete: set active = false
            MockTestMaster mockTest = mockTestOptional.get();
            mockTest.setActive(false);
            mockTestRepository.save(mockTest);

            baseResponse.setStatus(1);
            baseResponse.setCode(200);
            baseResponse.setMessage("Mock test deactivated successfully");
            baseResponse.setData(Collections.singletonList("Mock test with ID " + id + " has been deactivated successfully"));

        } catch (Exception exception) {
            log.error("Exception in deleteMockTestById: ", exception);
            baseResponse.setStatus(0);
            baseResponse.setCode(500);
            baseResponse.setMessage("Failed to delete mock test: " + exception.getMessage());
            baseResponse.setData(null);
        }
        return baseResponse;
    }

    private MockTestResponse mapToMockTestResponse(MockTestMaster test) {
        MockTestResponse response = new MockTestResponse();
        response.setId(test.getId());

        // Exam & Category mapping
        if (test.getExam() != null) {
            response.setExamId(test.getExam().getId());
            response.setExamName(test.getExam().getExamName());
            response.setExamCode(test.getExam().getExamCode());

            if (test.getExam().getExamCategory() != null) {
                response.setCategoryId(test.getExam().getExamCategory().getId());
                response.setCategoryName(test.getExam().getExamCategory().getCategoryName());
            }
        }

        // Test Series mapping
        if (test.getTestSeries() != null) {
            response.setTestSeriesId(test.getTestSeries().getId());
            response.setTestSeriesTitle(test.getTestSeries().getTitle());
        }

        response.setTestName(test.getTestName());
        response.setTestType(test.getTestType());
        response.setTotalQuestions(test.getTotalQuestions());
        response.setTotalMarks(test.getTotalMarks());
        response.setDurationMinutes(test.getDurationMinutes());
        response.setNegativeMark(test.getNegativeMark());
        response.setInstructions(test.getInstructions());
        response.setFree(test.getFree() != null ? test.getFree() : false);
        response.setActive(test.getActive());
        response.setCreatedBy(test.getCreatedBy());
        response.setCreatedAt(test.getCreatedAt());
        response.setUpdatedAt(test.getUpdatedAt());

        // Kitne logo ne attempt kiya (default 0 agar kisi ne attempt nahi kiya)
        Long attempts = 0L;
        Long candidates = 0L;
        if (test.getId() != null) {
            attempts = mockTestAttemptRepository.countByMockTest_Id(test.getId());
            candidates = mockTestAttemptRepository.countDistinctCandidatesByMockTestId(test.getId());
        }
        response.setTotalAttempts(attempts != null ? attempts : 0L);
        response.setTotalCandidatesAttempted(candidates != null ? candidates : 0L);

        return response;
    }

    @Override
    @Transactional
    public BaseResponse<MockTestSubmissionResponse> submitMockTest(MockTestSubmissionRequest request) {
        BaseResponse<MockTestSubmissionResponse> baseResponse = new BaseResponse<>();
        try {
            if (request == null || request.getMockTestId() == null) {
                baseResponse.setStatus(0);
                baseResponse.setCode(400);
                baseResponse.setMessage("Mock test ID is required");
                return baseResponse;
            }

            // 1. Get Candidate from LoginUserContext (or SecurityContext fallback)
            LoginUser loginUser = LoginUserContext.getLoginUser();
            Long candidateId = loginUser != null ? loginUser.getCandidateId() : null;
            Long userId = loginUser != null ? loginUser.getUserId() : null;

            if (loginUser == null) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
                    candidateId = cud.getCandidateId();
                    userId = cud.getUserId();
                }
            }

            CandidateProfile candidate = null;
            if (candidateId != null) {
                candidate = candidatesRepository.findById(candidateId).orElse(null);
            }
            if (candidate == null && userId != null) {
                candidate = candidatesRepository.findByUserId(userId).orElse(null);
            }

            if (candidate == null) {
                baseResponse.setStatus(0);
                baseResponse.setCode(401);
                baseResponse.setMessage("Candidate profile not found for logged in user. Please log in first.");
                return baseResponse;
            }

            // 2. Fetch Mock Test
            Optional<MockTestMaster> mockTestOpt = mockTestRepository.findById(request.getMockTestId());
            if (mockTestOpt.isEmpty()) {
                baseResponse.setStatus(0);
                baseResponse.setCode(404);
                baseResponse.setMessage("Mock test not found with ID: " + request.getMockTestId());
                return baseResponse;
            }
            MockTestMaster mockTest = mockTestOpt.get();

            // 3. Determine attempt number for this candidate
            Integer previousAttempts = mockTestAttemptRepository.countByCandidate_IdAndMockTest_Id(candidate.getId(), mockTest.getId());
            int attemptNumber = (previousAttempts != null ? previousAttempts : 0) + 1;

            // 4. Fetch all questions and options for this mock test
            List<QuestionMaster> allQuestions = questionRepository.findByMockTest_Id(mockTest.getId());
            Map<Long, QuestionMaster> questionMap = allQuestions.stream()
                    .collect(Collectors.toMap(QuestionMaster::getId, q -> q));

            List<Long> questionIds = allQuestions.stream().map(QuestionMaster::getId).collect(Collectors.toList());
            List<QuestionOption> allOptions = !questionIds.isEmpty()
                    ? questionOptionRepository.findByQuestion_IdIn(questionIds)
                    : Collections.emptyList();
            Map<Long, List<QuestionOption>> optionsByQuestionId = allOptions.stream()
                    .collect(Collectors.groupingBy(o -> o.getQuestion().getId()));

            // 5. Evaluate responses
            int totalAttempted = 0;
            int totalCorrect = 0;
            int totalIncorrect = 0;
            int totalSkipped = 0;
            double totalScore = 0.0;

            MockTestAttempt attempt = new MockTestAttempt();
            attempt.setCandidate(candidate);
            attempt.setMockTest(mockTest);
            attempt.setAttemptNumber(attemptNumber);
            int timeTaken = (request.getTotalTimeTakenSeconds() != null) ? request.getTotalTimeTakenSeconds() : 0;
            attempt.setStartTime(LocalDateTime.now().minusSeconds(timeTaken));
            attempt.setSubmitTime(LocalDateTime.now());
            attempt.setTotalTimeTakenSeconds(timeTaken);
            attempt.setStatus("SUBMITTED");

            List<MockTestQuestionAttempt> questionAttempts = new ArrayList<>();
            List<QuestionResultResponse> questionResults = new ArrayList<>();
            Set<Long> processedQuestionIds = new HashSet<>();

            if (request.getAnswers() != null) {
                for (QuestionAnswerRequest answer : request.getAnswers()) {
                    if (answer.getQuestionId() == null) continue;
                    processedQuestionIds.add(answer.getQuestionId());

                    QuestionMaster question = questionMap.get(answer.getQuestionId());
                    if (question == null) {
                        question = questionRepository.findById(answer.getQuestionId()).orElse(null);
                    }
                    if (question == null) continue;

                    List<QuestionOption> options = optionsByQuestionId.getOrDefault(question.getId(), Collections.emptyList());
                    QuestionOption correctOption = options.stream()
                            .filter(o -> Boolean.TRUE.equals(o.getCorrect()))
                            .findFirst()
                            .orElse(null);

                    QuestionOption selectedOption = null;
                    if (answer.getSelectedOptionId() != null) {
                        selectedOption = options.stream()
                                .filter(o -> o.getId().equals(answer.getSelectedOptionId()))
                                .findFirst()
                                .orElse(null);
                    }

                    MockTestQuestionAttempt qa = new MockTestQuestionAttempt();
                    qa.setMockTestAttempt(attempt);
                    qa.setQuestion(question);
                    qa.setSelectedOption(selectedOption);
                    qa.setTimeTakenSeconds(answer.getTimeTakenSeconds() != null ? answer.getTimeTakenSeconds() : 0);
                    qa.setIsMarkedForReview(Boolean.TRUE.equals(answer.getIsMarkedForReview()));

                    QuestionResultResponse qr = new QuestionResultResponse();
                    qr.setQuestionId(question.getId());
                    qr.setQuestionText(question.getQuestionText());
                    qr.setTimeTakenSeconds(qa.getTimeTakenSeconds());
                    qr.setExplanation(question.getExplanation());
                    if (correctOption != null) {
                        qr.setCorrectOptionId(correctOption.getId());
                        qr.setCorrectOptionText(correctOption.getOptionText());
                    }

                    if (selectedOption != null) {
                        totalAttempted++;
                        boolean isCorrect = Boolean.TRUE.equals(selectedOption.getCorrect());
                        qa.setIsCorrect(isCorrect);
                        qr.setIsCorrect(isCorrect);
                        qr.setSelectedOptionId(selectedOption.getId());
                        qr.setSelectedOptionText(selectedOption.getOptionText());

                        double marks = question.getMarks() != null ? question.getMarks() : 1.0;
                        double negMarks = question.getNegativeMarks() != null
                                ? question.getNegativeMarks()
                                : (mockTest.getNegativeMark() != null ? mockTest.getNegativeMark().doubleValue() : 0.0);

                        if (isCorrect) {
                            totalCorrect++;
                            qa.setMarksAwarded(marks);
                            qa.setAttemptStatus("CORRECT");
                            totalScore += marks;
                        } else {
                            totalIncorrect++;
                            qa.setMarksAwarded(-negMarks);
                            qa.setAttemptStatus("INCORRECT");
                            totalScore -= negMarks;
                        }
                    } else {
                        totalSkipped++;
                        qa.setIsCorrect(null);
                        qa.setMarksAwarded(0.0);
                        qa.setAttemptStatus(answer.getAttemptStatus() != null ? answer.getAttemptStatus() : "SKIPPED");

                        qr.setIsCorrect(null);
                        qr.setSelectedOptionId(null);
                        qr.setSelectedOptionText(null);
                    }

                    qr.setMarksAwarded(qa.getMarksAwarded());
                    qr.setAttemptStatus(qa.getAttemptStatus());

                    questionAttempts.add(qa);
                    questionResults.add(qr);
                }
            }

            // Include questions that were not present in request.getAnswers() as skipped
            for (QuestionMaster q : allQuestions) {
                if (!processedQuestionIds.contains(q.getId())) {
                    totalSkipped++;
                    List<QuestionOption> options = optionsByQuestionId.getOrDefault(q.getId(), Collections.emptyList());
                    QuestionOption correctOption = options.stream()
                            .filter(o -> Boolean.TRUE.equals(o.getCorrect()))
                            .findFirst()
                            .orElse(null);

                    MockTestQuestionAttempt qa = new MockTestQuestionAttempt();
                    qa.setMockTestAttempt(attempt);
                    qa.setQuestion(q);
                    qa.setSelectedOption(null);
                    qa.setTimeTakenSeconds(0);
                    qa.setIsCorrect(null);
                    qa.setIsMarkedForReview(false);
                    qa.setAttemptStatus("SKIPPED");
                    qa.setMarksAwarded(0.0);
                    questionAttempts.add(qa);

                    QuestionResultResponse qr = new QuestionResultResponse();
                    qr.setQuestionId(q.getId());
                    qr.setQuestionText(q.getQuestionText());
                    qr.setSelectedOptionId(null);
                    qr.setSelectedOptionText(null);
                    if (correctOption != null) {
                        qr.setCorrectOptionId(correctOption.getId());
                        qr.setCorrectOptionText(correctOption.getOptionText());
                    }
                    qr.setIsCorrect(null);
                    qr.setMarksAwarded(0.0);
                    qr.setTimeTakenSeconds(0);
                    qr.setAttemptStatus("SKIPPED");
                    qr.setExplanation(q.getExplanation());
                    questionResults.add(qr);
                }
            }

            int totalQuestionsCount = !allQuestions.isEmpty() ? allQuestions.size() : questionAttempts.size();
            Double totalMarksVal = mockTest.getTotalMarks() != null
                    ? mockTest.getTotalMarks().doubleValue()
                    : (double) totalQuestionsCount;
            double percentage = (totalMarksVal > 0) ? (totalScore / totalMarksVal) * 100 : 0.0;
            double accuracy = (totalAttempted > 0) ? ((double) totalCorrect / totalAttempted) * 100 : 0.0;

            attempt.setTotalQuestions(totalQuestionsCount);
            attempt.setTotalAttempted(totalAttempted);
            attempt.setTotalCorrect(totalCorrect);
            attempt.setTotalIncorrect(totalIncorrect);
            attempt.setTotalSkipped(totalSkipped);
            attempt.setScore(Math.round(totalScore * 100.0) / 100.0);
            attempt.setTotalMarks(totalMarksVal);
            attempt.setPercentage(Math.round(percentage * 100.0) / 100.0);
            attempt.setAccuracy(Math.round(accuracy * 100.0) / 100.0);

            MockTestAttempt savedAttempt = mockTestAttemptRepository.save(attempt);

            for (MockTestQuestionAttempt qa : questionAttempts) {
                qa.setMockTestAttempt(savedAttempt);
            }
            mockTestQuestionAttemptRepository.saveAll(questionAttempts);

            // Build Response
            MockTestSubmissionResponse response = new MockTestSubmissionResponse();
            response.setAttemptId(savedAttempt.getId());
            response.setMockTestId(mockTest.getId());
            response.setMockTestName(mockTest.getTestName());
            response.setCandidateId(candidate.getId());
            response.setCandidateName(candidate.getFullName());
            response.setAttemptNumber(attemptNumber);
            response.setTotalQuestions(totalQuestionsCount);
            response.setTotalAttempted(totalAttempted);
            response.setTotalCorrect(totalCorrect);
            response.setTotalIncorrect(totalIncorrect);
            response.setTotalSkipped(totalSkipped);
            response.setScore(savedAttempt.getScore());
            response.setTotalMarks(totalMarksVal);
            response.setPercentage(savedAttempt.getPercentage());
            response.setAccuracy(savedAttempt.getAccuracy());
            response.setTotalTimeTakenSeconds(savedAttempt.getTotalTimeTakenSeconds());
            response.setStatus(savedAttempt.getStatus());
            response.setSubmitTime(savedAttempt.getSubmitTime());
            response.setQuestionResults(questionResults);

            baseResponse.setStatus(1);
            baseResponse.setCode(200);
            baseResponse.setMessage("Mock test responses saved and evaluated successfully");
            baseResponse.setData(Collections.singletonList(response));
            return baseResponse;

        } catch (Exception e) {
            log.error("Error submitting mock test response: {}", e.getMessage(), e);
            baseResponse.setStatus(0);
            baseResponse.setCode(500);
            baseResponse.setMessage("Failed to submit mock test response: " + e.getMessage());
            return baseResponse;
        }
    }

    @Override
    public BaseResponse<MockTestSubmissionResponse> getAttemptResult(Long attemptId) {
        BaseResponse<MockTestSubmissionResponse> baseResponse = new BaseResponse<>();
        try {
            if (attemptId == null) {
                baseResponse.setStatus(0);
                baseResponse.setCode(400);
                baseResponse.setMessage("Attempt ID is required");
                return baseResponse;
            }

            Optional<MockTestAttempt> attemptOpt = mockTestAttemptRepository.findById(attemptId);
            if (attemptOpt.isEmpty()) {
                baseResponse.setStatus(0);
                baseResponse.setCode(404);
                baseResponse.setMessage("Mock test attempt not found with ID: " + attemptId);
                return baseResponse;
            }
            MockTestAttempt attempt = attemptOpt.get();

            List<MockTestQuestionAttempt> questionAttempts = mockTestQuestionAttemptRepository.findByMockTestAttempt_Id(attemptId);
            List<QuestionResultResponse> questionResults = new ArrayList<>();

            for (MockTestQuestionAttempt qa : questionAttempts) {
                QuestionResultResponse qr = new QuestionResultResponse();
                if (qa.getQuestion() != null) {
                    qr.setQuestionId(qa.getQuestion().getId());
                    qr.setQuestionText(qa.getQuestion().getQuestionText());
                    qr.setExplanation(qa.getQuestion().getExplanation());

                    List<QuestionOption> options = questionOptionRepository.findByQuestion_Id(qa.getQuestion().getId());
                    QuestionOption correctOption = options.stream()
                            .filter(o -> Boolean.TRUE.equals(o.getCorrect()))
                            .findFirst()
                            .orElse(null);
                    if (correctOption != null) {
                        qr.setCorrectOptionId(correctOption.getId());
                        qr.setCorrectOptionText(correctOption.getOptionText());
                    }
                }
                if (qa.getSelectedOption() != null) {
                    qr.setSelectedOptionId(qa.getSelectedOption().getId());
                    qr.setSelectedOptionText(qa.getSelectedOption().getOptionText());
                }
                qr.setIsCorrect(qa.getIsCorrect());
                qr.setMarksAwarded(qa.getMarksAwarded());
                qr.setTimeTakenSeconds(qa.getTimeTakenSeconds());
                qr.setAttemptStatus(qa.getAttemptStatus());
                questionResults.add(qr);
            }

            MockTestSubmissionResponse response = new MockTestSubmissionResponse();
            response.setAttemptId(attempt.getId());
            if (attempt.getMockTest() != null) {
                response.setMockTestId(attempt.getMockTest().getId());
                response.setMockTestName(attempt.getMockTest().getTestName());
            }
            if (attempt.getCandidate() != null) {
                response.setCandidateId(attempt.getCandidate().getId());
                response.setCandidateName(attempt.getCandidate().getFullName());
            }
            response.setAttemptNumber(attempt.getAttemptNumber());
            response.setTotalQuestions(attempt.getTotalQuestions());
            response.setTotalAttempted(attempt.getTotalAttempted());
            response.setTotalCorrect(attempt.getTotalCorrect());
            response.setTotalIncorrect(attempt.getTotalIncorrect());
            response.setTotalSkipped(attempt.getTotalSkipped());
            response.setScore(attempt.getScore());
            response.setTotalMarks(attempt.getTotalMarks());
            response.setPercentage(attempt.getPercentage());
            response.setAccuracy(attempt.getAccuracy());
            response.setTotalTimeTakenSeconds(attempt.getTotalTimeTakenSeconds());
            response.setStatus(attempt.getStatus());
            response.setSubmitTime(attempt.getSubmitTime());
            response.setQuestionResults(questionResults);

            baseResponse.setStatus(1);
            baseResponse.setCode(200);
            baseResponse.setMessage("Mock test attempt result fetched successfully");
            baseResponse.setData(Collections.singletonList(response));
            return baseResponse;
        } catch (Exception e) {
            log.error("Error fetching attempt result: {}", e.getMessage(), e);
            baseResponse.setStatus(0);
            baseResponse.setCode(500);
            baseResponse.setMessage("Failed to fetch attempt result: " + e.getMessage());
            return baseResponse;
        }
    }
}
