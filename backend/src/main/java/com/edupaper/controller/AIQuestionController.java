package com.edupaper.controller;

import com.edupaper.dto.ai.AIGeneratedQuestionResponse;
import com.edupaper.dto.ai.GenerateQuestionRequest;
import com.edupaper.entity.User;
import com.edupaper.repository.UserRepository;
import com.edupaper.service.AIQuestionGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/questions")
@RequiredArgsConstructor
public class AIQuestionController {

    private final AIQuestionGenerationService
            aiQuestionGenerationService;

    private final UserRepository userRepository;


    @PostMapping("/generate")
    public ResponseEntity<AIGeneratedQuestionResponse>
    generateQuestion(
            @Valid @RequestBody
            GenerateQuestionRequest request,
            Authentication authentication
    ) {

        Long userId =
                getCurrentUserId(authentication);

        return ResponseEntity.ok(
                aiQuestionGenerationService
                        .generateQuestion(
                                request,
                                userId
                        )
        );
    }


    private Long getCurrentUserId(
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow();

        return user.getId();
    }
}