package com.edupaper.dto.blueprint;

import com.edupaper.entity.BlueprintConstraintType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlueprintConstraintRequest {

    @NotNull(message = "Constraint type is required")
    private BlueprintConstraintType constraintType;

    @NotBlank(message = "Constraint value is required")
    private String value;

    @NotNull(message = "Required count is required")
    @Min(value = 1, message = "Required count must be at least 1")
    private Integer requiredCount;
}