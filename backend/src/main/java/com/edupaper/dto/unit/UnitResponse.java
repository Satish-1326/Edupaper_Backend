package com.edupaper.dto.unit;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UnitResponse {

    private Long id;

    private Integer unitNumber;

    private String title;

    private String description;

    private Long subjectId;
}