package com.edupaper.dto.bloom;

import com.edupaper.entity.BloomLevel;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloomValidationResponse {

    private Long questionId;

    private String questionText;

    private BloomLevel assignedLevel;

    private BloomLevel detectedLevel;

    private boolean matched;

    private double confidence;

    private String explanation;
}