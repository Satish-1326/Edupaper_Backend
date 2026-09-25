package com.edupaper.dto.paper;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReorderPaperQuestionsRequest {

    @NotEmpty(message = "Question order cannot be empty")
    private List<@NotNull(message = "Question ID cannot be null") Long> questionIds;
}