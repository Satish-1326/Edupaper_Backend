package com.edupaper.dto.document;

import com.edupaper.entity.DocumentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadedDocumentResponse {

    private Long id;

    private String fileName;

    private String fileType;

    private Long fileSize;

    private DocumentStatus status;

    private Long subjectId;

    private Long uploadedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}