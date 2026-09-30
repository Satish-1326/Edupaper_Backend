package com.edupaper.repository;

import com.edupaper.entity.UploadedDocument;
import com.edupaper.entity.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UploadedDocumentRepository
        extends JpaRepository<UploadedDocument, Long> {

    List<UploadedDocument>
    findByUploadedByIdOrderByCreatedAtDesc(Long userId);

    Optional<UploadedDocument>
    findByIdAndUploadedById(Long documentId, Long userId);

    List<UploadedDocument>
    findBySubjectIdAndUploadedByIdOrderByCreatedAtDesc(
            Long subjectId,
            Long userId
    );

    List<UploadedDocument>
    findByStatusAndUploadedById(
            DocumentStatus status,
            Long userId
    );
}