package com.edupaper.repository;

import com.edupaper.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository
        extends JpaRepository<Subject, Long> {

    List<Subject> findByCreatedById(Long userId);

    Optional<Subject> findByIdAndCreatedById(
            Long subjectId,
            Long userId
    );

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(
            String code,
            Long id
    );
}