package com.edupaper.dto.courseoutcome;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseOutcomeResponse {

    private Long id;

    private Integer coNumber;

    private String description;

    private Long subjectId;
}