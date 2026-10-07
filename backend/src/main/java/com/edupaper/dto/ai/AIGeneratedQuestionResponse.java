package com.edupaper.dto.ai;

import com.edupaper.entity.BloomLevel;
import com.edupaper.entity.Difficulty;
import com.edupaper.entity.QuestionType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIGeneratedQuestionResponse {

    private String questionText;

    private String answer;

    private String explanation;

    private QuestionType questionType;

    private Difficulty difficulty;

    private Integer marks;

    private BloomLevel bloomLevel;

    private Long unitId;

    private Integer unitNumber;

    private String unitTitle;

    private String source;
}