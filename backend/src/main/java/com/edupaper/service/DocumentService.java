package com.edupaper.service;

import com.edupaper.dto.document.UploadedDocumentResponse;
import com.edupaper.entity.DocumentStatus;
import com.edupaper.entity.Subject;
import com.edupaper.entity.UploadedDocument;
import com.edupaper.entity.User;
import com.edupaper.exception.BadRequestException;
import com.edupaper.exception.ResourceNotFoundException;
import com.edupaper.repository.SubjectRepository;
import com.edupaper.repository.UploadedDocumentRepository;
import com.edupaper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final UploadedDocumentRepository documentRepository;

    private final SubjectRepository subjectRepository;

    private final UserRepository userRepository;

    @Value("${edupaper.upload.directory:uploads}")
    private String uploadDirectory;

    public UploadedDocumentResponse uploadDocument(
            MultipartFile file,
            Long subjectId,
            Long userId
    ) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException(
                    "Please select a PDF file."
            );
        }

        String contentType = file.getContentType();

        if (!"application/pdf".equalsIgnoreCase(contentType)) {
            throw new BadRequestException(
                    "Only PDF files are supported."
            );
        }

        if (file.getOriginalFilename() == null ||
                !file.getOriginalFilename()
                        .toLowerCase()
                        .endsWith(".pdf")) {

            throw new BadRequestException(
                    "Only .pdf files are supported."
            );
        }

        Subject subject =
                subjectRepository
                        .findByIdAndCreatedById(
                                subjectId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Subject not found"
                                )
                        );

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        try {

            Path uploadPath =
                    Paths.get(uploadDirectory)
                            .toAbsolutePath()
                            .normalize();

            Files.createDirectories(uploadPath);

            String originalName =
                    Paths.get(
                                    file.getOriginalFilename()
                            )
                            .getFileName()
                            .toString();

            String storedFileName =
                    UUID.randomUUID()
                            + "_"
                            + originalName;

            Path targetPath =
                    uploadPath.resolve(
                            storedFileName
                    ).normalize();

            if (!targetPath.startsWith(uploadPath)) {
                throw new BadRequestException(
                        "Invalid file name."
                );
            }

            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            UploadedDocument document =
                    UploadedDocument.builder()
                            .fileName(originalName)
                            .fileType("application/pdf")
                            .fileSize(file.getSize())
                            .filePath(
                                    targetPath.toString()
                            )
                            .status(
                                    DocumentStatus.UPLOADED
                            )
                            .subject(subject)
                            .uploadedBy(user)
                            .build();

            UploadedDocument saved =
                    documentRepository.save(
                            document
                    );

            return mapToResponse(saved);

        } catch (IOException e) {

            throw new BadRequestException(
                    "Failed to save PDF file."
            );
        }
    }

    public List<UploadedDocumentResponse> getAll(
            Long userId
    ) {

        return documentRepository
                .findByUploadedByIdOrderByCreatedAtDesc(
                        userId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UploadedDocumentResponse getById(
            Long documentId,
            Long userId
    ) {

        UploadedDocument document =
                documentRepository
                        .findByIdAndUploadedById(
                                documentId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Document not found"
                                )
                        );

        return mapToResponse(document);
    }

    public void delete(
            Long documentId,
            Long userId
    ) {

        UploadedDocument document =
                documentRepository
                        .findByIdAndUploadedById(
                                documentId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Document not found"
                                )
                        );

        try {

            Files.deleteIfExists(
                    Paths.get(
                            document.getFilePath()
                    )
            );

        } catch (IOException e) {

            throw new BadRequestException(
                    "Failed to delete PDF file."
            );
        }

        documentRepository.delete(document);
    }

    private UploadedDocumentResponse mapToResponse(
            UploadedDocument document
    ) {

        return UploadedDocumentResponse.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .fileType(document.getFileType())
                .fileSize(document.getFileSize())
                .status(document.getStatus())
                .subjectId(
                        document.getSubject().getId()
                )
                .uploadedBy(
                        document.getUploadedBy().getId()
                )
                .createdAt(
                        document.getCreatedAt()
                )
                .updatedAt(
                        document.getUpdatedAt()
                )
                .build();
    }
}