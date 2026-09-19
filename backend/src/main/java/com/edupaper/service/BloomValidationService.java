package com.edupaper.service;

import com.edupaper.dto.bloom.BloomValidationResponse;
import com.edupaper.entity.BloomLevel;
import com.edupaper.entity.Question;
import com.edupaper.exception.ResourceNotFoundException;
import com.edupaper.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class BloomValidationService {

    private final QuestionRepository questionRepository;


    @Transactional(readOnly = true)
    public BloomValidationResponse validateQuestion(
            Long questionId,
            Long userId
    ) {

        Question question = questionRepository
                .findByIdAndCreatedById(questionId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Question not found or you do not have access"
                        )
                );

        return analyzeQuestion(question);
    }


    private BloomValidationResponse analyzeQuestion(
            Question question
    ) {

        String text = question.getQuestionText()
                .toLowerCase()
                .trim();

        BloomScoreResult result = detectBloomLevel(text);

        BloomLevel assignedLevel = question.getBloomLevel();

        boolean matched =
                assignedLevel == result.level;


        return BloomValidationResponse.builder()
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .assignedLevel(assignedLevel)
                .detectedLevel(result.level)
                .matched(matched)
                .confidence(result.confidence)
                .explanation(result.explanation)
                .build();
    }


    private BloomScoreResult detectBloomLevel(
            String text
    ) {

        Map<BloomLevel, Integer> scores =
                new EnumMap<>(BloomLevel.class);

        for (BloomLevel level : BloomLevel.values()) {
            scores.put(level, 0);
        }


        // =====================================================
        // L1 - REMEMBER
        // =====================================================

        addScore(
                text,
                scores,
                BloomLevel.L1_REMEMBER,
                List.of(
                        "define",
                        "what is",
                        "what are",
                        "list",
                        "name",
                        "identify",
                        "state",
                        "recall",
                        "mention",
                        "describe"
                ),
                3
        );


        // =====================================================
        // L2 - UNDERSTAND
        // =====================================================

        addScore(
                text,
                scores,
                BloomLevel.L2_UNDERSTAND,
                List.of(
                        "explain",
                        "summarize",
                        "interpret",
                        "illustrate",
                        "classify",
                        "compare",
                        "discuss",
                        "differentiate",
                        "in your own words"
                ),
                3
        );


        // =====================================================
        // L3 - APPLY
        // =====================================================

        addScore(
                text,
                scores,
                BloomLevel.L3_APPLY,
                List.of(
                        "apply",
                        "calculate",
                        "solve",
                        "use",
                        "implement",
                        "demonstrate",
                        "compute",
                        "execute",
                        "find the result"
                ),
                4
        );


        // =====================================================
        // L4 - ANALYZE
        // =====================================================

        addScore(
                text,
                scores,
                BloomLevel.L4_ANALYZE,
                List.of(
                        "analyze",
                        "analyse",
                        "examine",
                        "break down",
                        "investigate",
                        "distinguish",
                        "differentiate",
                        "compare and contrast",
                        "identify relationships",
                        "analyze the results"
                ),
                5
        );


        // =====================================================
        // L5 - EVALUATE
        // =====================================================

        addScore(
                text,
                scores,
                BloomLevel.L5_EVALUATE,
                List.of(
                        "evaluate",
                        "justify",
                        "critique",
                        "judge",
                        "assess",
                        "defend",
                        "recommend",
                        "argue",
                        "validate",
                        "which is better",
                        "justify your answer"
                ),
                6
        );


        // =====================================================
        // L6 - CREATE
        // =====================================================

        addScore(
                text,
                scores,
                BloomLevel.L6_CREATE,
                List.of(
                        "design",
                        "create",
                        "develop",
                        "construct",
                        "formulate",
                        "propose",
                        "build",
                        "design a solution",
                        "develop a model",
                        "create a system"
                ),
                7
        );


        // Find highest score
        BloomLevel detectedLevel = scores
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(BloomLevel.L1_REMEMBER);


        int highestScore = scores.get(detectedLevel);


        // Calculate simple confidence
        double confidence;

        if (highestScore == 0) {
            confidence = 0.25;
        } else {
            confidence = Math.min(
                    0.95,
                    0.50 + (highestScore * 0.08)
            );
        }


        String explanation =
                generateExplanation(
                        detectedLevel,
                        highestScore
                );


        return new BloomScoreResult(
                detectedLevel,
                confidence,
                explanation
        );
    }


    private void addScore(
            String text,
            Map<BloomLevel, Integer> scores,
            BloomLevel level,
            List<String> keywords,
            int weight
    ) {

        for (String keyword : keywords) {

            if (containsPhrase(text, keyword)) {

                scores.put(
                        level,
                        scores.get(level) + weight
                );
            }
        }
    }


    private boolean containsPhrase(
            String text,
            String phrase
    ) {

        String regex =
                "(?i)(?<!\\w)"
                        + Pattern.quote(phrase)
                        + "(?!\\w)";

        return Pattern
                .compile(regex)
                .matcher(text)
                .find();
    }


    private String generateExplanation(
            BloomLevel level,
            int score
    ) {

        if (score == 0) {

            return "No strong Bloom's Taxonomy action verb was detected.";
        }

        return switch (level) {

            case L1_REMEMBER ->
                    "The question primarily requires recall of facts, definitions, or basic information.";

            case L2_UNDERSTAND ->
                    "The question primarily requires explanation, interpretation, comparison, or understanding of concepts.";

            case L3_APPLY ->
                    "The question primarily requires applying a concept, procedure, formula, or technique.";

            case L4_ANALYZE ->
                    "The question primarily requires examining components, relationships, patterns, or differences.";

            case L5_EVALUATE ->
                    "The question primarily requires making a judgment, assessment, recommendation, or justification.";

            case L6_CREATE ->
                    "The question primarily requires designing, constructing, developing, or proposing something new.";
        };
    }


    private record BloomScoreResult(
            BloomLevel level,
            double confidence,
            String explanation
    ) {
    }
}