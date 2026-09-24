package com.edupaper.dto.paper;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaperQuestionResponse {

    private Long id;

    private Integer questionOrder;

    private Long questionId;

    private String questionText;

    private String questionType;

    private String difficulty;

    private Integer marks;

    private String bloomLevel;
}