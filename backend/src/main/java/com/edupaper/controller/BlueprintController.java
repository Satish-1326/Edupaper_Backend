package com.edupaper.controller;

import com.edupaper.dto.blueprint.BlueprintResponse;
import com.edupaper.dto.blueprint.CreateBlueprintRequest;
import com.edupaper.entity.User;
import com.edupaper.repository.UserRepository;
import com.edupaper.service.BlueprintService;

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
public class BlueprintController {

    private final BlueprintService blueprintService;
    private final UserRepository userRepository;

    // =========================
    // CREATE BLUEPRINT
    // =========================

    @PostMapping("/subjects/{subjectId}/blueprints")
    public ResponseEntity<BlueprintResponse> createBlueprint(
            @PathVariable Long subjectId,
            @Valid @RequestBody CreateBlueprintRequest request,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        BlueprintResponse response =
                blueprintService.createBlueprint(
                        subjectId,
                        request,
                        userId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =========================
    // GET ALL BLUEPRINTS
    // =========================

    @GetMapping("/blueprints")
    public ResponseEntity<List<BlueprintResponse>> getAllBlueprints(
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        return ResponseEntity.ok(
                blueprintService.getAllBlueprints(userId)
        );
    }

    // =========================
    // GET BLUEPRINT BY ID
    // =========================

    @GetMapping("/blueprints/{blueprintId}")
    public ResponseEntity<BlueprintResponse> getBlueprint(
            @PathVariable Long blueprintId,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        return ResponseEntity.ok(
                blueprintService.getBlueprint(
                        blueprintId,
                        userId
                )
        );
    }

    // =========================
    // GET BLUEPRINTS BY SUBJECT
    // =========================

    @GetMapping("/subjects/{subjectId}/blueprints")
    public ResponseEntity<List<BlueprintResponse>> getBlueprintsBySubject(
            @PathVariable Long subjectId,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        return ResponseEntity.ok(
                blueprintService.getBlueprintsBySubject(
                        subjectId,
                        userId
                )
        );
    }

    // =========================
    // UPDATE BLUEPRINT
    // =========================

    @PutMapping("/blueprints/{blueprintId}")
    public ResponseEntity<BlueprintResponse> updateBlueprint(
            @PathVariable Long blueprintId,
            @Valid @RequestBody CreateBlueprintRequest request,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        return ResponseEntity.ok(
                blueprintService.updateBlueprint(
                        blueprintId,
                        request,
                        userId
                )
        );
    }

    // =========================
    // DELETE BLUEPRINT
    // =========================

    @DeleteMapping("/blueprints/{blueprintId}")
    public ResponseEntity<Void> deleteBlueprint(
            @PathVariable Long blueprintId,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        blueprintService.deleteBlueprint(
                blueprintId,
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

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"));

        return user.getId();
    }
}