package com.helixion.sarkariPariksha.Services;

import com.helixion.sarkariPariksha.Request.StudyGoalRequest;
import com.helixion.sarkariPariksha.Response.DayScheduleResponse;
import com.helixion.sarkariPariksha.Response.MonthlyScheduleResponse;
import com.helixion.sarkariPariksha.Response.StudyGoalResponse;
import com.helixion.sarkariPariksha.Response.StudyPlannerSummaryResponse;
import com.helixion.sarkariPariksha.Utils.BaseResponse;

import java.time.LocalDate;
import java.util.List;

public interface StudyGoalServices {

    // 1. Create or Update Goal / Target (e.g. from popup modal)
    BaseResponse<StudyGoalResponse> saveGoal(StudyGoalRequest request);

    // 2. Toggle Complete status (checkbox / mark done)
    BaseResponse<StudyGoalResponse> toggleGoalCompletion(Long id);

    // 3. Delete / Deactivate Goal
    BaseResponse<String> deleteGoal(Long id);

    // 4. Get Goal by ID
    BaseResponse<StudyGoalResponse> getGoalById(Long id);

    // 5. Get Schedule for a specific Day (with optional category filter e.g. Mocks, Revision, Exams)
    BaseResponse<DayScheduleResponse> getScheduleByDate(LocalDate date, String category);

    // 6. Get Calendar dots / monthly breakdown for a specific Year & Month
    BaseResponse<MonthlyScheduleResponse> getMonthlySchedule(Integer year, Integer month);

    // 7. Get Active Exam Countdowns
    BaseResponse<List<StudyGoalResponse>> getExamCountdowns();

    // 8. Get Planner Summary (top banners: days left, mocks progress, streak)
    BaseResponse<StudyPlannerSummaryResponse> getPlannerSummary();
}
