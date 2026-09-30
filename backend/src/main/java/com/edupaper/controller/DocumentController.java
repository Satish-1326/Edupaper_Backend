package com.edupaper.controller;

import com.edupaper.dto.document.UploadedDocumentResponse;
import com.edupaper.entity.User;
import com.edupaper.repository.UserRepository;
import com.edupaper.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.edupaper.dto.document.DocumentProcessingResponse;
import com.edupaper.service.PdfProcessingService;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    private final UserRepository userRepository;

    private final PdfProcessingService pdfProcessingService;

    @PostMapping("/upload")
    public ResponseEntity<UploadedDocumentResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subjectId") Long subjectId,
            Authentication authentication
    ) {

        Long userId =
                getCurrentUserId(authentication);

        return ResponseEntity.ok(
                documentService.uploadDocument(
                        file,
                        subjectId,
                        userId
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<UploadedDocumentResponse>> getAll(
            Authentication authentication
    ) {

        Long userId =
                getCurrentUserId(authentication);

        return ResponseEntity.ok(
                documentService.getAll(userId)
        );
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<UploadedDocumentResponse> getById(
            @PathVariable Long documentId,
            Authentication authentication
    ) {

        Long userId =
                getCurrentUserId(authentication);

        return ResponseEntity.ok(
                documentService.getById(
                        documentId,
                        userId
                )
        );
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long documentId,
            Authentication authentication
    ) {

        Long userId =
                getCurrentUserId(authentication);

        documentService.delete(
                documentId,
                userId
        );

        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId(
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow();

        return user.getId();
    }

    @PostMapping("/{documentId}/process")
    public ResponseEntity<DocumentProcessingResponse> processDocument(
            @PathVariable Long documentId,
            Authentication authentication
    ) {

        Long userId =
                getCurrentUserId(authentication);

        return ResponseEntity.ok(
                pdfProcessingService.processDocument(
                        documentId,
                        userId
                )
        );
    }
}