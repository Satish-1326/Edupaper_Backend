package com.edupaper.repository;

import com.edupaper.entity.Blueprint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlueprintRepository extends JpaRepository<Blueprint, Long> {

    List<Blueprint> findByCreatedByIdOrderByCreatedAtDesc(Long userId);

    Optional<Blueprint> findByIdAndCreatedById(Long blueprintId, Long userId);

    List<Blueprint> findBySubjectIdAndCreatedByIdOrderByCreatedAtDesc(
            Long subjectId,
            Long userId
    );
}