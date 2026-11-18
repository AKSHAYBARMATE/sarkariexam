package com.helixion.sarkariPariksha.Repository;

import com.helixion.sarkariPariksha.Entity.StudyGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StudyGoalRepository extends JpaRepository<StudyGoal, Long> {

    List<StudyGoal> findByCandidate_IdAndActiveTrue(Long candidateId);

    List<StudyGoal> findByCandidate_IdAndTargetDateAndActiveTrue(Long candidateId, LocalDate targetDate);

    List<StudyGoal> findByCandidate_IdAndTargetDateAndCategoryAndActiveTrue(Long candidateId, LocalDate targetDate, String category);

    @Query("SELECT g FROM StudyGoal g WHERE g.candidate.id = :candidateId AND g.targetDate BETWEEN :startDate AND :endDate AND g.active = true ORDER BY g.targetDate ASC, g.timeSlot ASC")
    List<StudyGoal> findByCandidateIdAndDateRange(
            @Param("candidateId") Long candidateId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT g FROM StudyGoal g WHERE g.candidate.id = :candidateId AND (g.category = 'EXAM_DATE' OR g.category = 'Exam Date') AND g.active = true ORDER BY g.targetDate ASC")
    List<StudyGoal> findActiveExamCountdowns(@Param("candidateId") Long candidateId);

    Long countByCandidate_IdAndTargetDateAndActiveTrue(Long candidateId, LocalDate targetDate);

    Long countByCandidate_IdAndTargetDateAndCompletedTrueAndActiveTrue(Long candidateId, LocalDate targetDate);
}
