package com.edupaper.dto.paper;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplacePaperQuestionRequest {

    @NotNull(message = "New question ID is required")
    private Long newQuestionId;
}