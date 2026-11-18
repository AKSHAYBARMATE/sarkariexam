package com.helixion.sarkariPariksha.Controller;

import com.helixion.sarkariPariksha.Request.StudyGoalRequest;
import com.helixion.sarkariPariksha.Response.DayScheduleResponse;
import com.helixion.sarkariPariksha.Response.MonthlyScheduleResponse;
import com.helixion.sarkariPariksha.Response.StudyGoalResponse;
import com.helixion.sarkariPariksha.Response.StudyPlannerSummaryResponse;
import com.helixion.sarkariPariksha.Services.StudyGoalServices;
import com.helixion.sarkariPariksha.Utils.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("StudyGoal")
@Tag(name = "Study Goal & Planner Controller", description = "APIs for Study Schedule, Goal Planner, Target Exam Countdowns and Calendar")
public class StudyGoalController {

    @Autowired
    private StudyGoalServices studyGoalServices;

    @PostMapping("/saveGoal")
    @Operation(summary = "Add or Update Study Goal / Target", description = "Creates or updates a target or study event (e.g. Daily Quiz, Revision, Exam Date) from the modal popup.")
    public BaseResponse<StudyGoalResponse> saveGoal(@RequestBody StudyGoalRequest request) {
        return studyGoalServices.saveGoal(request);
    }

    @PostMapping("/toggleComplete/{id}")
    @Operation(summary = "Toggle Target Completion", description = "Toggles target status between completed and pending.")
    public BaseResponse<StudyGoalResponse> toggleGoalCompletion(@PathVariable Long id) {
        return studyGoalServices.toggleGoalCompletion(id);
    }

    @DeleteMapping("/deleteGoal/{id}")
    @Operation(summary = "Delete Target", description = "Deactivates a study target by its ID.")
    public BaseResponse<String> deleteGoal(@PathVariable Long id) {
        return studyGoalServices.deleteGoal(id);
    }

    @GetMapping("/getGoalById/{id}")
    @Operation(summary = "Get Target By ID", description = "Fetches a specific study goal details by its ID.")
    public BaseResponse<StudyGoalResponse> getGoalById(@PathVariable Long id) {
        return studyGoalServices.getGoalById(id);
    }

    @GetMapping("/getDaySchedule")
    @Operation(summary = "Get Schedule for a Specific Day", description = "Fetches schedule and target completion statistics for a given day (defaults to today). Supports category filtering: All, Mocks, Revision, Exams.")
    public BaseResponse<DayScheduleResponse> getScheduleByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String category) {
        return studyGoalServices.getScheduleByDate(date, category);
    }

    @GetMapping("/getMonthlySchedule")
    @Operation(summary = "Get Monthly Calendar Schedule & Dots", description = "Returns calendar dots (Mock, Exam, Revision) for each day of the specified month/year to render in calendar.")
    public BaseResponse<MonthlyScheduleResponse> getMonthlySchedule(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return studyGoalServices.getMonthlySchedule(year, month);
    }

    @GetMapping("/getExamCountdowns")
    @Operation(summary = "Get Target Exam Countdowns", description = "Fetches list of active exam goals with days left, mock progress, and preparation percentage.")
    public BaseResponse<List<StudyGoalResponse>> getExamCountdowns() {
        return studyGoalServices.getExamCountdowns();
    }

    @GetMapping("/getPlannerSummary")
    @Operation(summary = "Get Study Planner Overview Summary", description = "Fetches top metrics: upcoming exam days left, mocks completed this month, streak, and active goals.")
    public BaseResponse<StudyPlannerSummaryResponse> getPlannerSummary() {
        return studyGoalServices.getPlannerSummary();
    }
}
