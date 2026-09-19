package com.edupaper.dto.blueprint;

import com.edupaper.entity.BlueprintConstraintType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlueprintConstraintResponse {

    private Long id;

    private BlueprintConstraintType constraintType;

    private String value;

    private Integer requiredCount;
}