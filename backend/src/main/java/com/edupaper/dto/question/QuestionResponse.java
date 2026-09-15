package com.edupaper.dto.question;

import com.edupaper.entity.BloomLevel;
import com.edupaper.entity.Difficulty;
import com.edupaper.entity.QuestionType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponse {

    private Long id;

    private String questionText;

    private String answer;

    private String explanation;

    private QuestionType questionType;

    private Difficulty difficulty;

    private Integer marks;

    private BloomLevel bloomLevel;

    private Long subjectId;

    private Long unitId;

    private Long topicId;

    private Long courseOutcomeId;

    private String source;

    private String tags;

    private List<QuestionOptionResponse> options;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}