package com.edupaper.controller;

import com.edupaper.dto.bloom.BloomValidationResponse;
import com.edupaper.entity.User;
import com.edupaper.repository.UserRepository;
import com.edupaper.service.BloomValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bloom")
@RequiredArgsConstructor
public class BloomValidationController {

    private final BloomValidationService bloomValidationService;
    private final UserRepository userRepository;


    @PostMapping("/validate/{questionId}")
    public ResponseEntity<BloomValidationResponse> validateQuestion(
            @PathVariable Long questionId,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        BloomValidationResponse response =
                bloomValidationService.validateQuestion(
                        questionId,
                        userId
                );

        return ResponseEntity.ok(response);
    }


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