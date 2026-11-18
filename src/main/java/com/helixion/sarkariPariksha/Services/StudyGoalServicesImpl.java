package com.helixion.sarkariPariksha.Services;

import com.helixion.sarkariPariksha.Entity.CandidateProfile;
import com.helixion.sarkariPariksha.Entity.ExamMaster;
import com.helixion.sarkariPariksha.Entity.StudyGoal;
import com.helixion.sarkariPariksha.Repository.CandidatesRepository;
import com.helixion.sarkariPariksha.Repository.ExamRepository;
import com.helixion.sarkariPariksha.Repository.StudyGoalRepository;
import com.helixion.sarkariPariksha.Request.StudyGoalRequest;
import com.helixion.sarkariPariksha.Response.DayScheduleResponse;
import com.helixion.sarkariPariksha.Response.MonthlyScheduleResponse;
import com.helixion.sarkariPariksha.Response.StudyGoalResponse;
import com.helixion.sarkariPariksha.Response.StudyPlannerSummaryResponse;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StudyGoalServicesImpl implements StudyGoalServices {

    @Autowired
    private StudyGoalRepository studyGoalRepository;

    @Autowired
    private CandidatesRepository candidatesRepository;

    @Autowired
    private ExamRepository examRepository;

    private CandidateProfile getAuthenticatedCandidate() {
        LoginUser loginUser = LoginUserContext.getLoginUser();
        Long candidateId = loginUser != null ? loginUser.getCandidateId() : null;
        Long userId = loginUser != null ? loginUser.getUserId() : null;

        if (candidateId == null && userId == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
                candidateId = cud.getCandidateId();
                userId = cud.getUserId();
            }
        }

        if (candidateId != null) {
            Optional<CandidateProfile> opt = candidatesRepository.findById(candidateId);
            if (opt.isPresent()) return opt.get();
        }

        if (userId != null) {
            Optional<CandidateProfile> opt = candidatesRepository.findByUserId(userId);
            if (opt.isPresent()) return opt.get();
        }

        return null;
    }

    private String normalizeCategory(String input) {
        if (input == null) return "STUDY_GOAL";
        String trimmed = input.trim().toUpperCase().replace(" ", "_");
        if (trimmed.contains("MOCK")) return "MOCK_TEST";
        if (trimmed.contains("REVISION")) return "REVISION";
        if (trimmed.contains("EXAM")) return "EXAM_DATE";
        return "STUDY_GOAL";
    }

    private String normalizePriority(String input) {
        if (input == null) return "MEDIUM";
        String trimmed = input.trim().toUpperCase();
        if (trimmed.equals("HIGH") || trimmed.equals("LOW")) return trimmed;
        return "MEDIUM";
    }

    @Override
    @Transactional
    public BaseResponse<StudyGoalResponse> saveGoal(StudyGoalRequest request) {
        BaseResponse<StudyGoalResponse> baseResponse = new BaseResponse<>();
        try {
            CandidateProfile candidate = getAuthenticatedCandidate();
            if (candidate == null) {
                baseResponse.setStatus(0);
                baseResponse.setCode(401);
                baseResponse.setMessage("Authenticated candidate not found. Please log in first.");
                return baseResponse;
            }

            if (request == null || request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                baseResponse.setStatus(0);
                baseResponse.setCode(400);
                baseResponse.setMessage("Goal / Event title is required");
                return baseResponse;
            }

            StudyGoal goal;
            if (request.getId() != null) {
                goal = studyGoalRepository.findById(request.getId()).orElse(new StudyGoal());
            } else {
                goal = new StudyGoal();
            }

            goal.setCandidate(candidate);
            goal.setTitle(request.getTitle().trim());
            goal.setCategory(normalizeCategory(request.getCategory()));
            goal.setTimeSlot(request.getTimeSlot() != null ? request.getTimeSlot().trim() : "08:00 AM");
            goal.setPriorityLevel(normalizePriority(request.getPriorityLevel()));
            goal.setDescription(request.getDescription());

            // Target Date resolution
            if (request.getTargetDate() != null) {
                goal.setTargetDate(request.getTargetDate());
            } else if (request.getDayOfMonth() != null) {
                int year = request.getYear() != null ? request.getYear() : LocalDate.now().getYear();
                int month = request.getMonth() != null ? request.getMonth() : LocalDate.now().getMonthValue();
                goal.setTargetDate(LocalDate.of(year, month, request.getDayOfMonth()));
            } else {
                goal.setTargetDate(LocalDate.now());
            }

            // Optional Exam Mapping
            if (request.getExamId() != null) {
                Optional<ExamMaster> examOpt = examRepository.findById(request.getExamId());
                examOpt.ifPresent(goal::setExam);
            }

            if (request.getTotalMocks() != null) {
                goal.setTotalMocks(request.getTotalMocks());
            }
            if (request.getCompletedMocks() != null) {
                goal.setCompletedMocks(request.getCompletedMocks());
            }
            if (request.getPreparationPercentage() != null) {
                goal.setPreparationPercentage(request.getPreparationPercentage());
            } else if (goal.getTotalMocks() != null && goal.getTotalMocks() > 0 && goal.getCompletedMocks() != null) {
                goal.setPreparationPercentage(Math.round(((double) goal.getCompletedMocks() / goal.getTotalMocks()) * 1000.0) / 10.0);
            }

            StudyGoal saved = studyGoalRepository.save(goal);

            baseResponse.setStatus(1);
            baseResponse.setCode(200);
            baseResponse.setMessage(request.getId() != null ? "Target updated successfully" : "Target saved successfully");
            baseResponse.setData(Collections.singletonList(mapToResponse(saved)));
            return baseResponse;
        } catch (Exception e) {
            log.error("Error saving study goal: {}", e.getMessage(), e);
            baseResponse.setStatus(0);
            baseResponse.setCode(500);
            baseResponse.setMessage("Failed to save target: " + e.getMessage());
            return baseResponse;
        }
    }

    @Override
    @Transactional
    public BaseResponse<StudyGoalResponse> toggleGoalCompletion(Long id) {
        BaseResponse<StudyGoalResponse> baseResponse = new BaseResponse<>();
        try {
            CandidateProfile candidate = getAuthenticatedCandidate();
            if (candidate == null) {
                baseResponse.setStatus(0);
                baseResponse.setCode(401);
                baseResponse.setMessage("Authenticated candidate not found. Please log in first.");
                return baseResponse;
            }

            Optional<StudyGoal> opt = studyGoalRepository.findById(id);
            if (opt.isEmpty()) {
                baseResponse.setStatus(0);
                baseResponse.setCode(404);
                baseResponse.setMessage("Target not found with ID: " + id);
                return baseResponse;
            }

            StudyGoal goal = opt.get();
            boolean newStatus = !Boolean.TRUE.equals(goal.getCompleted());
            goal.setCompleted(newStatus);
            goal.setCompletedAt(newStatus ? LocalDateTime.now() : null);

            StudyGoal saved = studyGoalRepository.save(goal);

            baseResponse.setStatus(1);
            baseResponse.setCode(200);
            baseResponse.setMessage("Target marked as " + (newStatus ? "Completed" : "Incomplete"));
            baseResponse.setData(Collections.singletonList(mapToResponse(saved)));
            return baseResponse;
        } catch (Exception e) {
            log.error("Error toggling target completion: {}", e.getMessage(), e);
            baseResponse.setStatus(0);
            baseResponse.setCode(500);
            baseResponse.setMessage("Failed to update status: " + e.getMessage());
            return baseResponse;
        }
    }

    @Override
    @Transactional
    public BaseResponse<String> deleteGoal(Long id) {
        BaseResponse<String> baseResponse = new BaseResponse<>();
        try {
            Optional<StudyGoal> opt = studyGoalRepository.findById(id);
            if (opt.isEmpty()) {
                baseResponse.setStatus(0);
                baseResponse.setCode(404);
                baseResponse.setMessage("Target not found with ID: " + id);
                return baseResponse;
            }

            StudyGoal goal = opt.get();
            goal.setActive(false);
            studyGoalRepository.save(goal);

            baseResponse.setStatus(1);
            baseResponse.setCode(200);
            baseResponse.setMessage("Target deleted successfully");
            baseResponse.setData(Collections.singletonList("Target ID " + id + " deactivated"));
            return baseResponse;
        } catch (Exception e) {
            log.error("Error deleting target: {}", e.getMessage(), e);
            baseResponse.setStatus(0);
            baseResponse.setCode(500);
            baseResponse.setMessage("Failed to delete target: " + e.getMessage());
            return baseResponse;
        }
    }

    @Override
    public BaseResponse<StudyGoalResponse> getGoalById(Long id) {
        BaseResponse<StudyGoalResponse> baseResponse = new BaseResponse<>();
        try {
            Optional<StudyGoal> opt = studyGoalRepository.findById(id);
            if (opt.isEmpty() || !Boolean.TRUE.equals(opt.get().getActive())) {
                baseResponse.setStatus(0);
                baseResponse.setCode(404);
                baseResponse.setMessage("Target not found with ID: " + id);
                return baseResponse;
            }

            baseResponse.setStatus(1);
            baseResponse.setCode(200);
            baseResponse.setMessage("Target fetched successfully");
            baseResponse.setData(Collections.singletonList(mapToResponse(opt.get())));
            return baseResponse;
        } catch (Exception e) {
            log.error("Error fetching target by ID: {}", e.getMessage(), e);
            baseResponse.setStatus(0);
            baseResponse.setCode(500);
            baseResponse.setMessage("Failed to fetch target: " + e.getMessage());
            return baseResponse;
        }
    }

    @Override
    public BaseResponse<DayScheduleResponse> getScheduleByDate(LocalDate date, String category) {
        BaseResponse<DayScheduleResponse> baseResponse = new BaseResponse<>();
        try {
            CandidateProfile candidate = getAuthenticatedCandidate();
            if (candidate == null) {
                baseResponse.setStatus(0);
                baseResponse.setCode(401);
                baseResponse.setMessage("Authenticated candidate not found. Please log in first.");
                return baseResponse;
            }

            LocalDate targetDate = (date != null) ? date : LocalDate.now();

            List<StudyGoal> goals;
            if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
                String normalizedCat = normalizeCategory(category);
                goals = studyGoalRepository.findByCandidate_IdAndTargetDateAndCategoryAndActiveTrue(candidate.getId(), targetDate, normalizedCat);
            } else {
                goals = studyGoalRepository.findByCandidate_IdAndTargetDateAndActiveTrue(candidate.getId(), targetDate);
            }

            int total = goals.size();
            int completed = (int) goals.stream().filter(g -> Boolean.TRUE.equals(g.getCompleted())).count();
            double percentage = total > 0 ? Math.round(((double) completed / total) * 100.0) : 0.0;

            DayScheduleResponse response = new DayScheduleResponse();
            response.setDate(targetDate);
            response.setDayOfMonth(targetDate.getDayOfMonth());
            response.setTotalTargets(total);
            response.setCompletedTargets(completed);
            response.setCompletionPercentage(percentage);
            response.setTargets(goals.stream().map(this::mapToResponse).collect(Collectors.toList()));

            baseResponse.setStatus(1);
            baseResponse.setCode(200);
            baseResponse.setMessage("Day schedule fetched successfully");
            baseResponse.setData(Collections.singletonList(response));
            return baseResponse;
        } catch (Exception e) {
            log.error("Error fetching day schedule: {}", e.getMessage(), e);
            baseResponse.setStatus(0);
            baseResponse.setCode(500);
            baseResponse.setMessage("Failed to fetch schedule: " + e.getMessage());
            return baseResponse;
        }
    }

    @Override
    public BaseResponse<MonthlyScheduleResponse> getMonthlySchedule(Integer year, Integer month) {
        BaseResponse<MonthlyScheduleResponse> baseResponse = new BaseResponse<>();
        try {
            CandidateProfile candidate = getAuthenticatedCandidate();
            if (candidate == null) {
                baseResponse.setStatus(0);
                baseResponse.setCode(401);
                baseResponse.setMessage("Authenticated candidate not found. Please log in first.");
                return baseResponse;
            }

            int yr = (year != null) ? year : LocalDate.now().getYear();
            int mo = (month != null) ? month : LocalDate.now().getMonthValue();

            LocalDate startDate = LocalDate.of(yr, mo, 1);
            LocalDate endDate = startDate.plusMonths(1).minusDays(1);

            List<StudyGoal> monthGoals = studyGoalRepository.findByCandidateIdAndDateRange(candidate.getId(), startDate, endDate);

            Map<Integer, List<String>> dotsMap = new HashMap<>();
            int totalMocks = 0;
            int totalRevisions = 0;
            int totalExams = 0;

            for (StudyGoal g : monthGoals) {
                int day = g.getTargetDate().getDayOfMonth();
                dotsMap.computeIfAbsent(day, k -> new ArrayList<>());
                if (!dotsMap.get(day).contains(g.getCategory())) {
                    dotsMap.get(day).add(g.getCategory());
                }

                if ("MOCK_TEST".equals(g.getCategory())) totalMocks++;
                else if ("REVISION".equals(g.getCategory())) totalRevisions++;
                else if ("EXAM_DATE".equals(g.getCategory())) totalExams++;
            }

            MonthlyScheduleResponse response = new MonthlyScheduleResponse();
            response.setYear(yr);
            response.setMonth(mo);
            response.setDayDots(dotsMap);
            response.setTotalMocksThisMonth(totalMocks);
            response.setTotalRevisionsThisMonth(totalRevisions);
            response.setTotalExamsThisMonth(totalExams);

            baseResponse.setStatus(1);
            baseResponse.setCode(200);
            baseResponse.setMessage("Monthly calendar schedule fetched successfully");
            baseResponse.setData(Collections.singletonList(response));
            return baseResponse;
        } catch (Exception e) {
            log.error("Error fetching monthly schedule: {}", e.getMessage(), e);
            baseResponse.setStatus(0);
            baseResponse.setCode(500);
            baseResponse.setMessage("Failed to fetch monthly schedule: " + e.getMessage());
            return baseResponse;
        }
    }

    @Override
    public BaseResponse<List<StudyGoalResponse>> getExamCountdowns() {
        BaseResponse<List<StudyGoalResponse>> baseResponse = new BaseResponse<>();
        try {
            CandidateProfile candidate = getAuthenticatedCandidate();
            if (candidate == null) {
                baseResponse.setStatus(0);
                baseResponse.setCode(401);
                baseResponse.setMessage("Authenticated candidate not found. Please log in first.");
                return baseResponse;
            }

            List<StudyGoal> countdowns = studyGoalRepository.findActiveExamCountdowns(candidate.getId());
            List<StudyGoalResponse> responses = countdowns.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            baseResponse.setStatus(1);
            baseResponse.setCode(200);
            baseResponse.setMessage("Active exam countdowns fetched successfully");
            baseResponse.setData(Collections.singletonList(responses));
            return baseResponse;
        } catch (Exception e) {
            log.error("Error fetching exam countdowns: {}", e.getMessage(), e);
            baseResponse.setStatus(0);
            baseResponse.setCode(500);
            baseResponse.setMessage("Failed to fetch countdowns: " + e.getMessage());
            return baseResponse;
        }
    }

    @Override
    public BaseResponse<StudyPlannerSummaryResponse> getPlannerSummary() {
        BaseResponse<StudyPlannerSummaryResponse> baseResponse = new BaseResponse<>();
        try {
            CandidateProfile candidate = getAuthenticatedCandidate();
            if (candidate == null) {
                baseResponse.setStatus(0);
                baseResponse.setCode(401);
                baseResponse.setMessage("Authenticated candidate not found. Please log in first.");
                return baseResponse;
            }

            List<StudyGoal> countdowns = studyGoalRepository.findActiveExamCountdowns(candidate.getId());

            StudyPlannerSummaryResponse response = new StudyPlannerSummaryResponse();
            List<StudyGoalResponse> goalResponses = countdowns.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            response.setActiveExamGoals(goalResponses);

            // Primary countdown: Closest upcoming exam
            LocalDate today = LocalDate.now();
            Optional<StudyGoal> closestExam = countdowns.stream()
                    .filter(g -> !g.getTargetDate().isBefore(today))
                    .min(Comparator.comparing(StudyGoal::getTargetDate));

            if (closestExam.isPresent()) {
                StudyGoal exam = closestExam.get();
                response.setPrimaryCountdownExamName(exam.getTitle());
                response.setPrimaryCountdownDaysLeft(ChronoUnit.DAYS.between(today, exam.getTargetDate()));
            } else if (!countdowns.isEmpty()) {
                StudyGoal exam = countdowns.get(0);
                response.setPrimaryCountdownExamName(exam.getTitle());
                response.setPrimaryCountdownDaysLeft(ChronoUnit.DAYS.between(today, exam.getTargetDate()));
            }

            // Mocks progress this month
            int currentMonth = today.getMonthValue();
            int currentYear = today.getYear();
            LocalDate monthStart = LocalDate.of(currentYear, currentMonth, 1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            List<StudyGoal> monthMockGoals = studyGoalRepository.findByCandidateIdAndDateRange(candidate.getId(), monthStart, monthEnd)
                    .stream()
                    .filter(g -> "MOCK_TEST".equals(g.getCategory()))
                    .toList();

            int completedMocks = (int) monthMockGoals.stream().filter(g -> Boolean.TRUE.equals(g.getCompleted())).count();
            int targetMocks = Math.max(monthMockGoals.size(), 20); // default to 20 if less

            response.setMocksCompletedThisMonth(completedMocks);
            response.setTargetMocksThisMonth(targetMocks);
            response.setStudyStreakDays(7); // Can be dynamically calculated or configured

            baseResponse.setStatus(1);
            baseResponse.setCode(200);
            baseResponse.setMessage("Study planner summary fetched successfully");
            baseResponse.setData(Collections.singletonList(response));
            return baseResponse;
        } catch (Exception e) {
            log.error("Error fetching planner summary: {}", e.getMessage(), e);
            baseResponse.setStatus(0);
            baseResponse.setCode(500);
            baseResponse.setMessage("Failed to fetch planner summary: " + e.getMessage());
            return baseResponse;
        }
    }

    private StudyGoalResponse mapToResponse(StudyGoal goal) {
        StudyGoalResponse res = new StudyGoalResponse();
        res.setId(goal.getId());
        if (goal.getCandidate() != null) {
            res.setCandidateId(goal.getCandidate().getId());
            res.setCandidateName(goal.getCandidate().getFullName());
        }
        res.setTitle(goal.getTitle());
        res.setCategory(goal.getCategory());
        res.setTargetDate(goal.getTargetDate());
        if (goal.getTargetDate() != null) {
            res.setDayOfMonth(goal.getTargetDate().getDayOfMonth());
            res.setDaysLeft(ChronoUnit.DAYS.between(LocalDate.now(), goal.getTargetDate()));
        }
        res.setTimeSlot(goal.getTimeSlot());
        res.setPriorityLevel(goal.getPriorityLevel());
        res.setCompleted(goal.getCompleted());
        res.setCompletedAt(goal.getCompletedAt());

        if (goal.getExam() != null) {
            res.setExamId(goal.getExam().getId());
            res.setExamName(goal.getExam().getExamName());
            res.setExamCode(goal.getExam().getExamCode());
        }

        res.setTotalMocks(goal.getTotalMocks());
        res.setCompletedMocks(goal.getCompletedMocks());
        res.setPreparationPercentage(goal.getPreparationPercentage());
        res.setDescription(goal.getDescription());
        res.setActive(goal.getActive());
        res.setCreatedAt(goal.getCreatedAt());
        res.setUpdatedAt(goal.getUpdatedAt());
        return res;
    }
}
