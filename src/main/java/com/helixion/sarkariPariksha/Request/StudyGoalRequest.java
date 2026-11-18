package com.helixion.sarkariPariksha.Request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StudyGoalRequest {

    private Long id;

    // Goal or Event Title (e.g. "Daily 20-Min GA & CA Booster Quiz")
    private String title;

    // MOCK_TEST, REVISION, EXAM_DATE, STUDY_GOAL
    private String category;

    // Target Date (e.g. 2026-11-10)
    private LocalDate targetDate;

    // Convenience fields if frontend sends day / month / year separately
    private Integer dayOfMonth;
    private Integer month;
    private Integer year;

    // Time Slot (e.g. "08:00 AM")
    private String timeSlot;

    // Priority: HIGH, MEDIUM, LOW
    private String priorityLevel;

    // Optional Exam ID
    private Long examId;

    // Optional metrics for exam countdown cards
    private Integer totalMocks;
    private Integer completedMocks;
    private Double preparationPercentage;

    private String description;
}
