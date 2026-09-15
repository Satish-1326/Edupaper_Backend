package com.edupaper.dto.question;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionOptionRequest {

    @NotBlank(message = "Option label is required")
    private String optionLabel;

    @NotBlank(message = "Option text is required")
    private String optionText;

    private Boolean correct = false;
}
