package com.edupaper.repository;

import com.edupaper.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UnitRepository extends JpaRepository<Unit, Long> {

    List<Unit> findBySubjectIdOrderByUnitNumberAsc(Long subjectId);

    Optional<Unit> findByIdAndSubjectCreatedById(
            Long unitId,
            Long userId
    );

    boolean existsBySubjectIdAndUnitNumber(
            Long subjectId,
            Integer unitNumber
    );

    boolean existsBySubjectIdAndUnitNumberAndIdNot(
            Long subjectId,
            Integer unitNumber,
            Long unitId
    );
}