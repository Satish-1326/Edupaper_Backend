package com.edupaper.service;

import com.edupaper.entity.*;
import com.edupaper.exception.BadRequestException;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionSelectionService {

    /**
     * Select questions that satisfy:
     *
     * 1. Total question count
     * 2. All blueprint constraints simultaneously
     * 3. Exact total marks
     * 4. No duplicate questions
     * 5. Every constraint must be satisfied exactly
     */
    public List<Question> selectQuestions(
            List<Question> availableQuestions,
            Blueprint blueprint) {

        // =====================================================
        // BASIC VALIDATION
        // =====================================================

        if (blueprint == null) {
            throw new BadRequestException(
                    "Blueprint is required"
            );
        }

        if (availableQuestions == null ||
                availableQuestions.isEmpty()) {

            throw new BadRequestException(
                    "No questions available for paper generation"
            );
        }

        if (blueprint.getTotalQuestions() == null ||
                blueprint.getTotalQuestions() <= 0) {

            throw new BadRequestException(
                    "Blueprint must contain a valid total question count"
            );
        }

        if (blueprint.getTotalMarks() == null ||
                blueprint.getTotalMarks() <= 0) {

            throw new BadRequestException(
                    "Blueprint must contain a valid total marks value"
            );
        }

        int requiredQuestions =
                blueprint.getTotalQuestions();

        int requiredMarks =
                blueprint.getTotalMarks();

        // =====================================================
        // REMOVE DUPLICATE QUESTIONS
        // =====================================================

        List<Question> candidates =
                removeDuplicateQuestions(availableQuestions);

        if (candidates.size() < requiredQuestions) {

            throw new BadRequestException(
                    "Not enough unique questions available. " +
                            "Required: " + requiredQuestions +
                            ", Available: " + candidates.size()
            );
        }

        // =====================================================
        // GET BLUEPRINT CONSTRAINTS
        // =====================================================

        List<BlueprintConstraint> constraints =
                blueprint.getConstraint();

        // =====================================================
        // NO CONSTRAINT CASE
        // =====================================================

        if (constraints == null ||
                constraints.isEmpty()) {

            return selectWithoutConstraints(
                    candidates,
                    requiredQuestions,
                    requiredMarks
            );
        }

        // =====================================================
        // VALIDATE CONSTRAINTS
        // =====================================================

        validateConstraintConfiguration(
                constraints,
                requiredQuestions
        );

        // =====================================================
        // RANDOMIZE CANDIDATES
        // =====================================================

        Collections.shuffle(candidates);

        // =====================================================
        // BACKTRACKING SEARCH
        // =====================================================

        List<Question> selected =
                new ArrayList<>();

        Map<String, Integer> currentCounts =
                new HashMap<>();

        boolean found =
                search(
                        candidates,
                        constraints,
                        requiredQuestions,
                        requiredMarks,
                        0,
                        selected,
                        currentCounts,
                        0
                );

        // =====================================================
        // NO VALID COMBINATION
        // =====================================================

        if (!found) {

            throw new BadRequestException(
                    buildFailureMessage(
                            candidates,
                            constraints,
                            requiredQuestions,
                            requiredMarks
                    )
            );
        }

        // =====================================================
        // FINAL VALIDATION
        // =====================================================

        validateFinalSelection(
                selected,
                constraints,
                requiredQuestions,
                requiredMarks
        );

        return selected;
    }


    // =========================================================
    // SELECT WITHOUT CONSTRAINTS
    // =========================================================

    private List<Question> selectWithoutConstraints(
            List<Question> candidates,
            int requiredQuestions,
            int requiredMarks) {

        List<Question> shuffled =
                new ArrayList<>(candidates);

        Collections.shuffle(shuffled);

        /*
         * We cannot simply take the first N questions because
         * their marks may not equal the blueprint total marks.
         *
         * Therefore use a small backtracking search based on marks.
         */

        List<Question> selected =
                new ArrayList<>();

        boolean found =
                searchByMarks(
                        shuffled,
                        requiredQuestions,
                        requiredMarks,
                        0,
                        selected,
                        0
                );

        if (!found) {

            throw new BadRequestException(
                    "Unable to generate paper. " +
                            "Could not find " + requiredQuestions +
                            " questions with total marks " +
                            requiredMarks
            );
        }

        return selected;
    }


    // =========================================================
    // MARK-ONLY BACKTRACKING
    // =========================================================

    private boolean searchByMarks(
            List<Question> candidates,
            int requiredQuestions,
            int requiredMarks,
            int index,
            List<Question> selected,
            int currentMarks) {

        // ---------------------------------------------
        // SUCCESS
        // ---------------------------------------------

        if (selected.size() == requiredQuestions) {

            return currentMarks == requiredMarks;
        }

        // ---------------------------------------------
        // NO MORE QUESTIONS
        // ---------------------------------------------

        if (index >= candidates.size()) {
            return false;
        }

        // ---------------------------------------------
        // TOO MANY MARKS
        // ---------------------------------------------

        if (currentMarks > requiredMarks) {
            return false;
        }

        // ---------------------------------------------
        // NOT ENOUGH QUESTIONS LEFT
        // ---------------------------------------------

        int remaining =
                candidates.size() - index;

        int stillNeeded =
                requiredQuestions - selected.size();

        if (remaining < stillNeeded) {
            return false;
        }

        Question question =
                candidates.get(index);

        int questionMarks =
                getQuestionMarks(question);

        // ---------------------------------------------
        // TRY INCLUDING QUESTION
        // ---------------------------------------------

        if (currentMarks + questionMarks <= requiredMarks) {

            selected.add(question);

            if (searchByMarks(
                    candidates,
                    requiredQuestions,
                    requiredMarks,
                    index + 1,
                    selected,
                    currentMarks + questionMarks)) {

                return true;
            }

            selected.remove(
                    selected.size() - 1
            );
        }

        // ---------------------------------------------
        // TRY SKIPPING QUESTION
        // ---------------------------------------------

        return searchByMarks(
                candidates,
                requiredQuestions,
                requiredMarks,
                index + 1,
                selected,
                currentMarks
        );
    }


    // =========================================================
    // MAIN BACKTRACKING SEARCH
    // =========================================================

    private boolean search(
            List<Question> candidates,
            List<BlueprintConstraint> constraints,
            int requiredQuestions,
            int requiredMarks,
            int index,
            List<Question> selected,
            Map<String, Integer> currentCounts,
            int currentMarks) {

        // ---------------------------------------------
        // SUCCESS CONDITION
        // ---------------------------------------------

        if (selected.size() == requiredQuestions) {

            return currentMarks == requiredMarks
                    && allConstraintsSatisfied(
                    constraints,
                    currentCounts
            );
        }

        // ---------------------------------------------
        // NO MORE CANDIDATES
        // ---------------------------------------------

        if (index >= candidates.size()) {
            return false;
        }

        // ---------------------------------------------
        // MARKS EXCEEDED
        // ---------------------------------------------

        if (currentMarks > requiredMarks) {
            return false;
        }

        // ---------------------------------------------
        // NOT ENOUGH QUESTIONS REMAINING
        // ---------------------------------------------

        int remaining =
                candidates.size() - index;

        int stillNeeded =
                requiredQuestions - selected.size();

        if (remaining < stillNeeded) {
            return false;
        }

        // ---------------------------------------------
        // CURRENT QUESTION
        // ---------------------------------------------

        Question question =
                candidates.get(index);

        int questionMarks =
                getQuestionMarks(question);

        // ---------------------------------------------
        // TRY INCLUDING QUESTION
        // ---------------------------------------------

        if (canAddQuestion(
                question,
                constraints,
                currentCounts)) {

            selected.add(question);

            List<String> matchedKeys =
                    getMatchedConstraintKeys(
                            question,
                            constraints
                    );

            // Update constraint counts
            for (String key : matchedKeys) {

                currentCounts.merge(
                        key,
                        1,
                        Integer::sum
                );
            }

            // Continue search
            if (search(
                    candidates,
                    constraints,
                    requiredQuestions,
                    requiredMarks,
                    index + 1,
                    selected,
                    currentCounts,
                    currentMarks + questionMarks
            )) {

                return true;
            }

            // -----------------------------------------
            // BACKTRACK
            // -----------------------------------------

            selected.remove(
                    selected.size() - 1
            );

            for (String key : matchedKeys) {

                int count =
                        currentCounts.getOrDefault(
                                key,
                                0
                        );

                if (count <= 1) {

                    currentCounts.remove(key);

                } else {

                    currentCounts.put(
                            key,
                            count - 1
                    );
                }
            }
        }

        // ---------------------------------------------
        // SKIP CURRENT QUESTION
        // ---------------------------------------------

        return search(
                candidates,
                constraints,
                requiredQuestions,
                requiredMarks,
                index + 1,
                selected,
                currentCounts,
                currentMarks
        );
    }


    // =========================================================
    // CAN ADD QUESTION?
    // =========================================================

    private boolean canAddQuestion(
            Question question,
            List<BlueprintConstraint> constraints,
            Map<String, Integer> currentCounts) {

        /*
         * Group constraints by type.
         *
         * Example:
         *
         * UNIT:
         *   Unit 1 -> 3
         *   Unit 2 -> 3
         *   Unit 3 -> 2
         *   Unit 4 -> 2
         *
         * Since the total is 10, every selected question
         * must match one UNIT constraint.
         */

        Map<BlueprintConstraintType, List<BlueprintConstraint>>
                groupedConstraints =
                constraints.stream()
                        .collect(
                                Collectors.groupingBy(
                                        BlueprintConstraint::getConstraintType
                                )
                        );

        // =====================================================
        // CHECK EVERY CONSTRAINT TYPE
        // =====================================================

        for (Map.Entry<
                BlueprintConstraintType,
                List<BlueprintConstraint>> entry
                : groupedConstraints.entrySet()) {

            List<BlueprintConstraint> typeConstraints =
                    entry.getValue();

            boolean matchesAny =
                    false;

            for (BlueprintConstraint constraint
                    : typeConstraints) {

                if (matches(question, constraint)) {

                    matchesAny = true;

                    String key =
                            getConstraintKey(constraint);

                    int current =
                            currentCounts.getOrDefault(
                                    key,
                                    0
                            );

                    /*
                     * This particular constraint is already
                     * full.
                     */
                    if (current >=
                            constraint.getRequiredCount()) {

                        return false;
                    }
                }
            }

            /*
             * If a constraint type exists in the blueprint,
             * the question must match at least one constraint
             * from that type.
             */
            if (!matchesAny) {
                return false;
            }
        }

        return true;
    }


    // =========================================================
    // MATCH QUESTION AGAINST CONSTRAINT
    // =========================================================

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

            // ---------------------------------------------
            // UNIT
            // ---------------------------------------------

            case UNIT:

                return question.getUnit() != null
                        && question.getUnit()
                        .getId()
                        .equals(parseLong(value));

            // ---------------------------------------------
            // DIFFICULTY
            // ---------------------------------------------

            case DIFFICULTY:

                return question.getDifficulty() != null
                        && question.getDifficulty()
                        .name()
                        .equalsIgnoreCase(value);

            // ---------------------------------------------
            // BLOOM LEVEL
            // ---------------------------------------------

            case BLOOM_LEVEL:

                return question.getBloomLevel() != null
                        && question.getBloomLevel()
                        .name()
                        .equalsIgnoreCase(value);

            // ---------------------------------------------
            // QUESTION TYPE
            // ---------------------------------------------

            case QUESTION_TYPE:

                return question.getQuestionType() != null
                        && question.getQuestionType()
                        .name()
                        .equalsIgnoreCase(value);

            // ---------------------------------------------
            // COURSE OUTCOME
            // ---------------------------------------------

            case COURSE_OUTCOME:

                return question.getCourseOutcome() != null
                        && question.getCourseOutcome()
                        .getId()
                        .equals(parseLong(value));

            default:

                return false;
        }
    }


    // =========================================================
    // GET MATCHED CONSTRAINT KEYS
    // =========================================================

    private List<String> getMatchedConstraintKeys(
            Question question,
            List<BlueprintConstraint> constraints) {

        List<String> keys =
                new ArrayList<>();

        for (BlueprintConstraint constraint
                : constraints) {

            if (matches(question, constraint)) {

                keys.add(
                        getConstraintKey(constraint)
                );
            }
        }

        return keys;
    }


    // =========================================================
    // CREATE CONSTRAINT KEY
    // =========================================================

    private String getConstraintKey(
            BlueprintConstraint constraint) {

        return constraint.getConstraintType()
                .name()
                + ":"
                + constraint.getValue()
                .trim()
                .toUpperCase();
    }


    // =========================================================
    // CHECK ALL CONSTRAINTS
    // =========================================================

    private boolean allConstraintsSatisfied(
            List<BlueprintConstraint> constraints,
            Map<String, Integer> currentCounts) {

        for (BlueprintConstraint constraint
                : constraints) {

            String key =
                    getConstraintKey(constraint);

            int actual =
                    currentCounts.getOrDefault(
                            key,
                            0
                    );

            if (actual !=
                    constraint.getRequiredCount()) {

                return false;
            }
        }

        return true;
    }


    // =========================================================
    // FINAL SELECTION VALIDATION
    // =========================================================

    private void validateFinalSelection(
            List<Question> selected,
            List<BlueprintConstraint> constraints,
            int requiredQuestions,
            int requiredMarks) {

        // ---------------------------------------------
        // QUESTION COUNT
        // ---------------------------------------------

        if (selected.size() != requiredQuestions) {

            throw new BadRequestException(
                    "Paper generation failed. Required " +
                            requiredQuestions +
                            " questions, but selected " +
                            selected.size()
            );
        }

        // ---------------------------------------------
        // DUPLICATE CHECK
        // ---------------------------------------------

        Set<Long> questionIds =
                new HashSet<>();

        for (Question question : selected) {

            if (question.getId() != null &&
                    !questionIds.add(question.getId())) {

                throw new BadRequestException(
                        "Paper generation failed. " +
                                "Duplicate question detected: " +
                                question.getId()
                );
            }
        }

        // ---------------------------------------------
        // TOTAL MARKS
        // ---------------------------------------------

        int actualMarks =
                selected.stream()
                        .mapToInt(
                                this::getQuestionMarks
                        )
                        .sum();

        if (actualMarks != requiredMarks) {

            throw new BadRequestException(
                    "Paper generation failed. " +
                            "Required total marks: " +
                            requiredMarks +
                            ", but selected questions have " +
                            actualMarks +
                            " marks."
            );
        }

        // ---------------------------------------------
        // CONSTRAINT COUNT VALIDATION
        // ---------------------------------------------

        Map<String, Integer> actualCounts =
                new HashMap<>();

        for (Question question : selected) {

            for (BlueprintConstraint constraint
                    : constraints) {

                if (matches(question, constraint)) {

                    String key =
                            getConstraintKey(constraint);

                    actualCounts.merge(
                            key,
                            1,
                            Integer::sum
                    );
                }
            }
        }

        if (!allConstraintsSatisfied(
                constraints,
                actualCounts)) {

            throw new BadRequestException(
                    "Paper generation failed. " +
                            "Selected questions do not satisfy " +
                            "all blueprint constraints."
            );
        }
    }


    // =========================================================
    // VALIDATE CONSTRAINT CONFIGURATION
    // =========================================================

    private void validateConstraintConfiguration(
            List<BlueprintConstraint> constraints,
            int requiredQuestions) {

        if (constraints == null ||
                constraints.isEmpty()) {

            return;
        }

        Map<BlueprintConstraintType, Integer>
                totalByType =
                new HashMap<>();

        Set<String> uniqueKeys =
                new HashSet<>();

        for (BlueprintConstraint constraint
                : constraints) {

            if (constraint == null) {

                throw new BadRequestException(
                        "Blueprint contains an invalid constraint"
                );
            }

            if (constraint.getConstraintType() == null) {

                throw new BadRequestException(
                        "Blueprint constraint type is required"
                );
            }

            if (constraint.getValue() == null ||
                    constraint.getValue()
                            .trim()
                            .isEmpty()) {

                throw new BadRequestException(
                        "Blueprint constraint value is required"
                );
            }

            if (constraint.getRequiredCount() == null ||
                    constraint.getRequiredCount() <= 0) {

                throw new BadRequestException(
                        "Constraint required count must be greater than 0"
                );
            }

            String key =
                    getConstraintKey(constraint);

            if (!uniqueKeys.add(key)) {

                throw new BadRequestException(
                        "Duplicate blueprint constraint: "
                                + key
                );
            }

            totalByType.merge(
                    constraint.getConstraintType(),
                    constraint.getRequiredCount(),
                    Integer::sum
            );
        }

        /*
         * Every constraint type represents a complete
         * distribution of the paper.
         *
         * Example:
         *
         * UNIT:
         * 3 + 3 + 2 + 2 = 10
         *
         * DIFFICULTY:
         * 4 + 4 + 2 = 10
         *
         * Therefore every type must total the number
         * of questions in the paper.
         */

        for (Map.Entry<
                BlueprintConstraintType,
                Integer> entry
                : totalByType.entrySet()) {

            if (!entry.getValue()
                    .equals(requiredQuestions)) {

                throw new BadRequestException(
                        "Invalid "
                                + entry.getKey()
                                + " constraint distribution. "
                                + "Required count must total "
                                + requiredQuestions
                                + ", but found "
                                + entry.getValue()
                );
            }
        }
    }


    // =========================================================
    // REMOVE DUPLICATE QUESTIONS
    // =========================================================

    private List<Question> removeDuplicateQuestions(
            List<Question> questions) {

        Map<Long, Question> uniqueQuestions =
                new LinkedHashMap<>();

        List<Question> questionsWithoutId =
                new ArrayList<>();

        for (Question question : questions) {

            if (question == null) {
                continue;
            }

            if (question.getId() == null) {

                /*
                 * Normally persisted questions always have IDs.
                 * Keep null-ID questions separately instead of
                 * accidentally removing valid objects.
                 */
                questionsWithoutId.add(question);

            } else {

                uniqueQuestions.putIfAbsent(
                        question.getId(),
                        question
                );
            }
        }

        List<Question> result =
                new ArrayList<>(
                        uniqueQuestions.values()
                );

        result.addAll(
                questionsWithoutId
        );

        return result;
    }


    // =========================================================
    // GET QUESTION MARKS
    // =========================================================

    private int getQuestionMarks(
            Question question) {

        if (question == null ||
                question.getMarks() == null) {

            throw new BadRequestException(
                    "Question contains invalid marks"
            );
        }

        if (question.getMarks() <= 0) {

            throw new BadRequestException(
                    "Question marks must be greater than 0"
            );
        }

        return question.getMarks();
    }


    // =========================================================
    // PARSE LONG
    // =========================================================

    private Long parseLong(String value) {

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


    // =========================================================
    // FAILURE MESSAGE
    // =========================================================

    private String buildFailureMessage(
            List<Question> candidates,
            List<BlueprintConstraint> constraints,
            int requiredQuestions,
            int requiredMarks) {

        StringBuilder message =
                new StringBuilder();

        message.append(
                "Unable to generate a paper satisfying "
                        + "all blueprint requirements. "
        );

        message.append(
                "Required questions: "
        ).append(requiredQuestions).append(". ");

        message.append(
                "Required total marks: "
        ).append(requiredMarks).append(". ");

        message.append(
                "Available unique questions: "
        ).append(candidates.size()).append(". ");

        message.append(
                "The Question Bank does not contain "
                        + "a combination of questions that "
                        + "satisfies the requested question "
                        + "count, marks and constraints "
                        + "simultaneously."
        );

        return message.toString();
    }
}