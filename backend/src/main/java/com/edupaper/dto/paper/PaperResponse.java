package com.edupaper.dto.paper;

import com.edupaper.entity.PaperStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaperResponse {

    private Long id;

    private String name;

    private Long subjectId;

    private Long blueprintId;

    private Integer totalQuestions;

    private Integer totalMarks;

    private PaperStatus status;

    private Long createdBy;

    private List<PaperQuestionResponse> questions;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}