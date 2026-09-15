package com.edupaper.dto.question;

import com.edupaper.entity.BloomLevel;
import com.edupaper.entity.Difficulty;
import com.edupaper.entity.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateQuestionRequest {

    @NotBlank(message = "Question text is required")
    @Size(
            max = 5000,
            message = "Question text cannot exceed 5000 characters"
    )
    private String questionText;


    @Size(
            max = 5000,
            message = "Answer cannot exceed 5000 characters"
    )
    private String answer;


    @Size(
            max = 5000,
            message = "Explanation cannot exceed 5000 characters"
    )
    private String explanation;


    @NotNull(message = "Question type is required")
    private QuestionType questionType;


    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;


    @NotNull(message = "Marks are required")
    @Min(value = 1, message = "Marks must be at least 1")
    @Max(value = 100, message = "Marks cannot exceed 100")
    private Integer marks;


    @NotNull(message = "Bloom level is required")
    private BloomLevel bloomLevel;


    @NotNull(message = "Unit ID is required")
    private Long unitId;


    private Long topicId;


    private Long courseOutcomeId;


    @Size(
            max = 500,
            message = "Source cannot exceed 500 characters"
    )
    private String source;


    @Size(
            max = 1000,
            message = "Tags cannot exceed 1000 characters"
    )
    private String tags;


    @Valid
    private List<QuestionOptionRequest> options;
}