package com.edupaper.dto.document;

import com.edupaper.entity.DocumentStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentProcessingResponse {

    private Long documentId;

    private String fileName;

    private DocumentStatus status;

    private Integer totalUnitsDetected;

    private Integer totalCharacters;

    private List<UnitContentResponse> units;
}