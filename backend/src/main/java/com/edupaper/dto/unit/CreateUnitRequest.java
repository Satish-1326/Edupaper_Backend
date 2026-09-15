package com.edupaper.dto.unit;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUnitRequest {

    @NotNull(message = "Unit number is required")
    @Min(value = 1, message = "Unit number must be at least 1")
    private Integer unitNumber;

    @NotBlank(message = "Unit title is required")
    private String title;

    private String description;
}