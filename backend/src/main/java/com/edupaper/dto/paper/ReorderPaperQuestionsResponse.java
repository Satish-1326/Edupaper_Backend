package com.edupaper.dto.paper;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReorderPaperQuestionsResponse {

    private Long paperId;

    private Integer totalQuestions;

    private List<PaperQuestionOrderResponse> questions;

    private String message;
}