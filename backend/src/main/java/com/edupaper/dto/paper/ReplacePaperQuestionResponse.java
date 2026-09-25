package com.edupaper.dto.paper;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplacePaperQuestionResponse {

    private Long paperId;

    private Long oldQuestionId;

    private Long newQuestionId;

    private Integer questionOrder;

    private String questionText;

    private String questionType;

    private String difficulty;

    private Integer marks;

    private String bloomLevel;

    private Integer totalQuestions;

    private Integer currentTotalMarks;

    private String message;
}