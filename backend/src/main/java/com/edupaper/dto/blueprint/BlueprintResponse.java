package com.edupaper.dto.blueprint;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlueprintResponse {

    private Long id;

    private String name;

    private String description;

    private Integer totalQuestions;

    private Integer totalMarks;

    private Long subjectId;

    private Long createdBy;

    private List<BlueprintConstraintResponse> constraints;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}