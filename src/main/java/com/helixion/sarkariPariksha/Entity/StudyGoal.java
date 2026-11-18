package com.helixion.sarkariPariksha.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_goal")
@Data
public class StudyGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Kis candidate ka target/goal hai
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private CandidateProfile candidate;

    // Goal or Event Title (e.g. "Daily 20-Min GA & CA Booster Quiz", "SSC CGL 2025 Tier-1")
    @Column(name = "title", nullable = false)
    private String title;

    // Target Category: MOCK_TEST, REVISION, EXAM_DATE, STUDY_GOAL
    @Column(name = "category", length = 30, nullable = false)
    private String category;

    // Target Date (Day/Month/Year e.g. 2026-11-10)
    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    // Time Slot (e.g. "08:00 AM", "10:30 PM")
    @Column(name = "time_slot", length = 30)
    private String timeSlot;

    // Priority Level: HIGH, MEDIUM, LOW
    @Column(name = "priority_level", length = 20)
    private String priorityLevel = "MEDIUM";

    // Completed or Pending
    @Column(name = "is_completed")
    private Boolean completed = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Optional Exam Mapping (e.g. SSC CGL, RRB NTPC)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id")
    private ExamMaster exam;

    // Optional fields for Exam Countdown Goals
    @Column(name = "total_mocks")
    private Integer totalMocks;

    @Column(name = "completed_mocks")
    private Integer completedMocks = 0;

    @Column(name = "preparation_percentage")
    private Double preparationPercentage;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    private Boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.completed == null) {
            this.completed = false;
        }
        if (this.active == null) {
            this.active = true;
        }
        if (this.priorityLevel == null) {
            this.priorityLevel = "MEDIUM";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
