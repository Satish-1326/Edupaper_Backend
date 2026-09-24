package com.edupaper.controller;

import com.edupaper.dto.paper.GeneratePaperRequest;
import com.edupaper.dto.paper.PaperResponse;
import com.edupaper.entity.User;
import com.edupaper.repository.UserRepository;
import com.edupaper.service.PaperService;

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
public class PaperController {

    private final PaperService paperService;
    private final UserRepository userRepository;

    // =========================
    // GENERATE PAPER
    // =========================

    @PostMapping("/papers/generate")
    public ResponseEntity<PaperResponse> generatePaper(
            @Valid @RequestBody GeneratePaperRequest request,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        PaperResponse response =
                paperService.generatePaper(
                        request,
                        userId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =========================
    // GET ALL PAPERS
    // =========================

    @GetMapping("/papers")
    public ResponseEntity<List<PaperResponse>> getAllPapers(
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        return ResponseEntity.ok(
                paperService.getAllPapers(userId)
        );
    }

    // =========================
    // GET PAPER BY ID
    // =========================

    @GetMapping("/papers/{paperId}")
    public ResponseEntity<PaperResponse> getPaper(
            @PathVariable Long paperId,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        return ResponseEntity.ok(
                paperService.getPaper(
                        paperId,
                        userId
                )
        );
    }

    // =========================
    // GET PAPERS BY SUBJECT
    // =========================

    @GetMapping("/subjects/{subjectId}/papers")
    public ResponseEntity<List<PaperResponse>> getPapersBySubject(
            @PathVariable Long subjectId,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        return ResponseEntity.ok(
                paperService.getPapersBySubject(
                        subjectId,
                        userId
                )
        );
    }

    // =========================
    // DELETE PAPER
    // =========================

    @DeleteMapping("/papers/{paperId}")
    public ResponseEntity<Void> deletePaper(
            @PathVariable Long paperId,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        paperService.deletePaper(
                paperId,
                userId
        );

        return ResponseEntity.noContent().build();
    }

    // =========================
    // CURRENT USER
    // =========================

    private Long getCurrentUserId(
            Authentication authentication) {

        String email = authentication.getName();

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Authenticated user not found"
                                ));

        return user.getId();
    }
}