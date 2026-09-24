package com.edupaper.dto.paper;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratePaperRequest {

    @NotBlank(message = "Paper name is required")
    @Size(max = 255, message = "Paper name cannot exceed 255 characters")
    private String name;

    @NotNull(message = "Blueprint ID is required")
    private Long blueprintId;
}