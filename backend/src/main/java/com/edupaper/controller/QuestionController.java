package com.edupaper.controller;

import com.edupaper.dto.question.CreateQuestionRequest;
import com.edupaper.dto.question.QuestionResponse;
import com.edupaper.entity.User;
import com.edupaper.repository.UserRepository;
import com.edupaper.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final UserRepository userRepository;


    // =========================================================
    // CREATE QUESTION
    // =========================================================

    @PostMapping("/questions")
    public ResponseEntity<QuestionResponse> createQuestion(
            @Valid @RequestBody CreateQuestionRequest request,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        QuestionResponse response =
                questionService.createQuestion(
                        request,
                        userId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // =========================================================
    // GET ALL QUESTIONS
    // =========================================================

    @GetMapping("/questions")
    public ResponseEntity<List<QuestionResponse>> getAllQuestions(
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                questionService.getAllQuestions(userId)
        );
    }


    // =========================================================
    // GET ONE QUESTION
    // =========================================================

    @GetMapping("/questions/{questionId}")
    public ResponseEntity<QuestionResponse> getQuestion(
            @PathVariable Long questionId,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                questionService.getQuestion(
                        questionId,
                        userId
                )
        );
    }


    // =========================================================
    // GET QUESTIONS BY SUBJECT
    // =========================================================

    @GetMapping("/subjects/{subjectId}/questions")
    public ResponseEntity<List<QuestionResponse>> getQuestionsBySubject(
            @PathVariable Long subjectId,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                questionService.getQuestionsBySubject(
                        subjectId,
                        userId
                )
        );
    }


    // =========================================================
    // GET QUESTIONS BY UNIT
    // =========================================================

    @GetMapping("/units/{unitId}/questions")
    public ResponseEntity<List<QuestionResponse>> getQuestionsByUnit(
            @PathVariable Long unitId,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                questionService.getQuestionsByUnit(
                        unitId,
                        userId
                )
        );
    }


    // =========================================================
    // GET QUESTIONS BY TOPIC
    // =========================================================

    @GetMapping("/topics/{topicId}/questions")
    public ResponseEntity<List<QuestionResponse>> getQuestionsByTopic(
            @PathVariable Long topicId,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                questionService.getQuestionsByTopic(
                        topicId,
                        userId
                )
        );
    }


    // =========================================================
    // GET QUESTIONS BY COURSE OUTCOME
    // =========================================================

    @GetMapping("/course-outcomes/{courseOutcomeId}/questions")
    public ResponseEntity<List<QuestionResponse>>
    getQuestionsByCourseOutcome(
            @PathVariable Long courseOutcomeId,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                questionService.getQuestionsByCourseOutcome(
                        courseOutcomeId,
                        userId
                )
        );
    }


    // =========================================================
    // UPDATE QUESTION
    // =========================================================

    @PutMapping("/questions/{questionId}")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable Long questionId,
            @Valid @RequestBody CreateQuestionRequest request,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                questionService.updateQuestion(
                        questionId,
                        request,
                        userId
                )
        );
    }


    // =========================================================
    // DELETE QUESTION
    // =========================================================

    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long questionId,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        questionService.deleteQuestion(
                questionId,
                userId
        );

        return ResponseEntity.noContent().build();
    }


    // =========================================================
    // GET LOGGED-IN USER ID
    // =========================================================

    private Long getUserId(
            Authentication authentication
    ) {

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );

        return user.getId();
    }
}