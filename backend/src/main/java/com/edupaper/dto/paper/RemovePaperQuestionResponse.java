package com.edupaper.dto.paper;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemovePaperQuestionResponse {

    private Long paperId;
    private Long removedQuestionId;
    private Integer remainingQuestions;
    private Integer remainingMarks;
    private String message;
}