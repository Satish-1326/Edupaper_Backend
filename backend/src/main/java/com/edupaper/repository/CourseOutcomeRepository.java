package com.edupaper.repository;

import com.edupaper.entity.CourseOutcome;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseOutcomeRepository
        extends JpaRepository<CourseOutcome, Long> {

    List<CourseOutcome> findBySubjectIdOrderByCoNumberAsc(
            Long subjectId
    );

    Optional<CourseOutcome> findByIdAndSubjectCreatedById(
            Long coId,
            Long userId
    );
    Optional<CourseOutcome> findByIdAndSubjectIdAndSubjectCreatedById(
            Long coId,
            Long subjectId,
            Long userId
    );

    boolean existsBySubjectIdAndCoNumber(
            Long subjectId,
            Integer coNumber
    );

    boolean existsBySubjectIdAndCoNumberAndIdNot(
            Long subjectId,
            Integer coNumber,
            Long coId
    );
}