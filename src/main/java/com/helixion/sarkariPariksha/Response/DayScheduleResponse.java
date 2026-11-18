package com.helixion.sarkariPariksha.Response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DayScheduleResponse {

    private LocalDate date;
    private Integer dayOfMonth;
    private Integer totalTargets;
    private Integer completedTargets;
    private Double completionPercentage;
    private List<StudyGoalResponse> targets;
}
