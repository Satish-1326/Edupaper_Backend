package com.edupaper.dto.subject;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubjectResponse {

    private Long id;
    private String name;
    private String code;
    private String department;
    private Integer semester;
    private String academicYear;
    private Integer credits;
    private String description;
    private Long createdBy;
}