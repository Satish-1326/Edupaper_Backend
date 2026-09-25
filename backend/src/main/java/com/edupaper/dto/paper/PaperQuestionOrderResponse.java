package com.edupaper.dto.paper;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaperQuestionOrderResponse {

    private Integer questionOrder;

    private Long questionId;

    private String questionText;
}