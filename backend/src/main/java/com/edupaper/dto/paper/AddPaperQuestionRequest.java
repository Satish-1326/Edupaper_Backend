package com.edupaper.dto.paper;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddPaperQuestionRequest {

    @NotNull(message = "Question ID is required")
    private Long questionId;
}