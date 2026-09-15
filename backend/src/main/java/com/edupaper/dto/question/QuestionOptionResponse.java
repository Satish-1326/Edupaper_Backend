package com.edupaper.dto.question;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionOptionResponse {

    private Long id;

    private String optionLabel;

    private String optionText;

    private Boolean correct;
}