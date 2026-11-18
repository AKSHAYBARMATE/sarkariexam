package com.helixion.sarkariPariksha.Response;

import lombok.Data;

import java.util.List;

@Data
public class StudyPlannerSummaryResponse {

    // Top summary banners
    private String primaryCountdownExamName;
    private Long primaryCountdownDaysLeft;

    private Integer mocksCompletedThisMonth;
    private Integer targetMocksThisMonth;

    private Integer studyStreakDays;

    // Active Target Exam Countdowns
    private List<StudyGoalResponse> activeExamGoals;
}
