package com.edupaper.service;

import com.edupaper.entity.*;
import com.edupaper.exception.BadRequestException;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PaperValidationService {

    // =====================================================
    // MAIN VALIDATION
    // =====================================================

    public void validatePaper(Paper paper) {

        if (paper == null) {
            throw new BadRequestException(
                    "Paper is required"
            );
        }

        if (paper.getBlueprint() == null) {
            throw new BadRequestException(
                    "Paper does not have a blueprint"
            );
        }

        if (paper.getSubject() == null) {
            throw new BadRequestException(
                    "Paper does not have a subject"
            );
        }

        List<PaperQuestion> paperQuestions =
                paper.getQuestions();

        if (paperQuestions == null ||
                paperQuestions.isEmpty()) {

            throw new BadRequestException(
                    "Paper does not contain any questions"
            );
        }

        Blueprint blueprint =
                paper.getBlueprint();

        // =================================================
        // QUESTION COUNT
        // =================================================

        validateQuestionCount(
                paperQuestions,
                blueprint
        );

        // =================================================
        // DUPLICATE QUESTIONS
        // =================================================

        validateDuplicateQuestions(
                paperQuestions
        );

        // =================================================
        // SUBJECT VALIDATION
        // =================================================

        validateSubject(
                paper,
                paperQuestions
        );

        // =================================================
        // TOTAL MARKS
        // =================================================

        validateTotalMarks(
                paperQuestions,
                blueprint
        );

        // =================================================
        // BLUEPRINT CONSTRAINTS
        // =================================================

        validateConstraints(
                paperQuestions,
                blueprint
        );
    }


    // =====================================================
    // QUESTION COUNT
    // =====================================================

    private void validateQuestionCount(
            List<PaperQuestion> paperQuestions,
            Blueprint blueprint) {

        int requiredQuestions =
                blueprint.getTotalQuestions();

        int actualQuestions =
                paperQuestions.size();

        if (actualQuestions != requiredQuestions) {

            throw new BadRequestException(
                    "Paper must contain exactly "
                            + requiredQuestions
                            + " questions, but currently contains "
                            + actualQuestions
            );
        }
    }


    // =====================================================
    // DUPLICATE QUESTIONS
    // =====================================================

    private void validateDuplicateQuestions(
            List<PaperQuestion> paperQuestions) {

        Set<Long> questionIds =
                new HashSet<>();

        for (PaperQuestion paperQuestion :
                paperQuestions) {

            if (paperQuestion == null ||
                    paperQuestion.getQuestion() == null) {

                throw new BadRequestException(
                        "Paper contains an invalid question"
                );
            }

            Question question =
                    paperQuestion.getQuestion();

            if (question.getId() == null) {

                throw new BadRequestException(
                        "Paper contains a question without an ID"
                );
            }

            if (!questionIds.add(
                    question.getId())) {

                throw new BadRequestException(
                        "Duplicate question found in paper: "
                                + question.getId()
                );
            }
        }
    }


    // =====================================================
    // SUBJECT VALIDATION
    // =====================================================

    private void validateSubject(
            Paper paper,
            List<PaperQuestion> paperQuestions) {

        Long paperSubjectId =
                paper.getSubject().getId();

        if (paperSubjectId == null) {

            throw new BadRequestException(
                    "Paper subject is invalid"
            );
        }

        // Blueprint must belong to same subject
        if (paper.getBlueprint().getSubject() == null ||
                paper.getBlueprint()
                        .getSubject()
                        .getId() == null ||
                !paper.getBlueprint()
                        .getSubject()
                        .getId()
                        .equals(paperSubjectId)) {

            throw new BadRequestException(
                    "Paper and blueprint belong to different subjects"
            );
        }

        // Every question must belong to same subject
        for (PaperQuestion paperQuestion :
                paperQuestions) {

            Question question =
                    paperQuestion.getQuestion();

            if (question.getSubject() == null ||
                    question.getSubject().getId() == null) {

                throw new BadRequestException(
                        "Question "
                                + question.getId()
                                + " does not have a valid subject"
                );
            }

            if (!question.getSubject()
                    .getId()
                    .equals(paperSubjectId)) {

                throw new BadRequestException(
                        "Question "
                                + question.getId()
                                + " does not belong to the paper subject"
                );
            }
        }
    }


    // =====================================================
    // TOTAL MARKS
    // =====================================================

    private void validateTotalMarks(
            List<PaperQuestion> paperQuestions,
            Blueprint blueprint) {

        if (blueprint.getTotalMarks() == null ||
                blueprint.getTotalMarks() <= 0) {

            throw new BadRequestException(
                    "Blueprint contains invalid total marks"
            );
        }

        int actualMarks = 0;

        for (PaperQuestion paperQuestion :
                paperQuestions) {

            Question question =
                    paperQuestion.getQuestion();

            if (question.getMarks() == null ||
                    question.getMarks() <= 0) {

                throw new BadRequestException(
                        "Question "
                                + question.getId()
                                + " contains invalid marks"
                );
            }

            actualMarks += question.getMarks();
        }

        int requiredMarks =
                blueprint.getTotalMarks();

        if (actualMarks != requiredMarks) {

            throw new BadRequestException(
                    "Paper must contain exactly "
                            + requiredMarks
                            + " marks, but currently contains "
                            + actualMarks
                            + " marks"
            );
        }
    }


    // =====================================================
    // BLUEPRINT CONSTRAINTS
    // =====================================================

    private void validateConstraints(
            List<PaperQuestion> paperQuestions,
            Blueprint blueprint) {

        List<BlueprintConstraint> constraints =
                blueprint.getConstraint();

        if (constraints == null ||
                constraints.isEmpty()) {

            return;
        }

        Map<String, Integer> actualCounts =
                new HashMap<>();

        for (Question question :
                paperQuestions.stream()
                        .map(PaperQuestion::getQuestion)
                        .toList()) {

            for (BlueprintConstraint constraint :
                    constraints) {

                if (matches(
                        question,
                        constraint
                )) {

                    String key =
                            getConstraintKey(
                                    constraint
                            );

                    actualCounts.merge(
                            key,
                            1,
                            Integer::sum
                    );
                }
            }
        }

        for (BlueprintConstraint constraint :
                constraints) {

            String key =
                    getConstraintKey(
                            constraint
                    );

            int actual =
                    actualCounts.getOrDefault(
                            key,
                            0
                    );

            int required =
                    constraint.getRequiredCount();

            if (actual != required) {

                throw new BadRequestException(
                        "Blueprint constraint not satisfied: "
                                + constraint.getConstraintType()
                                + " = "
                                + constraint.getValue()
                                + ". Required: "
                                + required
                                + ", Actual: "
                                + actual
                );
            }
        }
    }


    // =====================================================
    // MATCH QUESTION WITH CONSTRAINT
    // =====================================================

    private boolean matches(
            Question question,
            BlueprintConstraint constraint) {

        if (question == null ||
                constraint == null ||
                constraint.getConstraintType() == null ||
                constraint.getValue() == null) {

            return false;
        }

        String value =
                constraint.getValue().trim();

        switch (constraint.getConstraintType()) {

            case UNIT:

                return question.getUnit() != null
                        && question.getUnit()
                        .getId()
                        .equals(parseLong(value));

            case DIFFICULTY:

                return question.getDifficulty() != null
                        && question.getDifficulty()
                        .name()
                        .equalsIgnoreCase(value);

            case BLOOM_LEVEL:

                return question.getBloomLevel() != null
                        && question.getBloomLevel()
                        .name()
                        .equalsIgnoreCase(value);

            case QUESTION_TYPE:

                return question.getQuestionType() != null
                        && question.getQuestionType()
                        .name()
                        .equalsIgnoreCase(value);

            case COURSE_OUTCOME:

                return question.getCourseOutcome() != null
                        && question.getCourseOutcome()
                        .getId()
                        .equals(parseLong(value));

            default:

                return false;
        }
    }


    // =====================================================
    // CONSTRAINT KEY
    // =====================================================

    private String getConstraintKey(
            BlueprintConstraint constraint) {

        return constraint.getConstraintType()
                .name()
                + ":"
                + constraint.getValue()
                .trim()
                .toUpperCase();
    }


    // =====================================================
    // PARSE LONG
    // =====================================================

    private Long parseLong(
            String value) {

        try {

            return Long.parseLong(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            throw new BadRequestException(
                    "Invalid numeric constraint value: "
                            + value
            );
        }
    }
}