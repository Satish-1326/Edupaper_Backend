package com.edupaper.dto.ai;

import com.edupaper.entity.BloomLevel;
import com.edupaper.entity.Difficulty;
import com.edupaper.entity.QuestionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateQuestionRequest {

    @NotNull
    private Long documentUnitContentId;

    @NotNull
    private QuestionType questionType;

    @NotNull
    private Difficulty difficulty;

    @NotNull
    private BloomLevel bloomLevel;

    @NotNull
    @Min(1)
    private Integer marks;
}