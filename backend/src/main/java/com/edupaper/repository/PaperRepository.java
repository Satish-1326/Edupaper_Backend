package com.edupaper.repository;

import com.edupaper.entity.Paper;
import com.edupaper.entity.PaperStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaperRepository extends JpaRepository<Paper, Long> {

    List<Paper> findByCreatedByIdOrderByCreatedAtDesc(Long userId);

    Optional<Paper> findByIdAndCreatedById(
            Long paperId,
            Long userId
    );

    List<Paper> findBySubjectIdAndCreatedByIdOrderByCreatedAtDesc(
            Long subjectId,
            Long userId
    );

    List<Paper> findByBlueprintIdAndCreatedByIdOrderByCreatedAtDesc(
            Long blueprintId,
            Long userId
    );

    List<Paper> findByStatusAndCreatedByIdOrderByCreatedAtDesc(
            PaperStatus status,
            Long userId
    );
}