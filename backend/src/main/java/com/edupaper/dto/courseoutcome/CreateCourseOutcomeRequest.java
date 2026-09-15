package com.edupaper.dto.courseoutcome;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseOutcomeRequest {

    @NotNull(message = "CO number is required")
    @Min(value = 1, message = "CO number must be at least 1")
    @Max(value = 20, message = "CO number cannot be greater than 20")
    private Integer coNumber;

    @NotBlank(message = "CO description is required")
    private String description;
}