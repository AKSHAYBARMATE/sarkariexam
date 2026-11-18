package com.helixion.sarkariPariksha.Repository;

import com.helixion.sarkariPariksha.Entity.MockTestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockTestAttemptRepository extends JpaRepository<MockTestAttempt, Long> {
    Long countByMockTest_Id(Long mockTestId);

    @Query("SELECT COUNT(DISTINCT m.candidate.id) FROM MockTestAttempt m WHERE m.mockTest.id = :mockTestId")
    Long countDistinctCandidatesByMockTestId(@Param("mockTestId") Long mockTestId);

    @Query("SELECT COUNT(m) FROM MockTestAttempt m WHERE m.mockTest.exam.id = :examId")
    Long countByExamId(@Param("examId") Long examId);

    @Query("SELECT COUNT(DISTINCT m.candidate.id) FROM MockTestAttempt m WHERE m.mockTest.exam.id = :examId")
    Long countDistinctCandidatesByExamId(@Param("examId") Long examId);

    Integer countByCandidate_IdAndMockTest_Id(Long candidateId, Long mockTestId);

    List<MockTestAttempt> findByCandidate_Id(Long candidateId);

    List<MockTestAttempt> findByCandidate_IdAndMockTest_Id(Long candidateId, Long mockTestId);
}
