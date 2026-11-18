package com.helixion.sarkariPariksha.Repository;

import com.helixion.sarkariPariksha.Entity.MockTestQuestionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockTestQuestionAttemptRepository extends JpaRepository<MockTestQuestionAttempt, Long> {

    List<MockTestQuestionAttempt> findByMockTestAttempt_Id(Long attemptId);

    void deleteByMockTestAttempt_Id(Long attemptId);
}
