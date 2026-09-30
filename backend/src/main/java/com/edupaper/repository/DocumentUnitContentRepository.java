package com.edupaper.repository;

import com.edupaper.entity.DocumentUnitContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentUnitContentRepository
        extends JpaRepository<DocumentUnitContent, Long> {

    List<DocumentUnitContent>
    findByDocumentIdOrderByUnitUnitNumberAsc(
            Long documentId
    );

    Optional<DocumentUnitContent>
    findByDocumentIdAndUnitId(
            Long documentId,
            Long unitId
    );

    void deleteByDocumentId(Long documentId);
}