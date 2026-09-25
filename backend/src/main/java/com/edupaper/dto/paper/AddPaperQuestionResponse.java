package com.edupaper.dto.paper;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddPaperQuestionResponse {

    private Long paperId;

    private Long addedQuestionId;

    private Integer questionOrder;

    private String questionText;

    private Integer questionMarks;

    private Integer totalQuestions;

    private Integer currentTotalMarks;

    private String message;
}