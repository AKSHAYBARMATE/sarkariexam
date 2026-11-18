package com.helixion.sarkariPariksha.Response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StudyGoalResponse {

    private Long id;
    private Long candidateId;
    private String candidateName;

    private String title;
    private String category;
    private LocalDate targetDate;
    private Integer dayOfMonth;
    private String timeSlot;
    private String priorityLevel;

    private Boolean completed;
    private LocalDateTime completedAt;

    // Exam countdown fields
    private Long examId;
    private String examName;
    private String examCode;
    private Long daysLeft;
    private Integer totalMocks;
    private Integer completedMocks;
    private Double preparationPercentage;

    private String description;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
