package com.edupaper.controller;

import com.edupaper.dto.unit.CreateUnitRequest;
import com.edupaper.dto.unit.UnitResponse;
import com.edupaper.service.UnitService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    @PostMapping("/api/subjects/{subjectId}/units")
    public ResponseEntity<UnitResponse> createUnit(
            @PathVariable Long subjectId,
            @Valid @RequestBody CreateUnitRequest request,
            Authentication authentication
    ) {

        UnitResponse response = unitService.createUnit(
                subjectId,
                request,
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/api/subjects/{subjectId}/units")
    public ResponseEntity<List<UnitResponse>> getUnitsBySubject(
            @PathVariable Long subjectId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                unitService.getUnitsBySubject(
                        subjectId,
                        authentication.getName()
                )
        );
    }

    @GetMapping("/api/units/{id}")
    public ResponseEntity<UnitResponse> getUnit(
            @PathVariable Long id,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                unitService.getUnit(
                        id,
                        authentication.getName()
                )
        );
    }

    @PutMapping("/api/units/{id}")
    public ResponseEntity<UnitResponse> updateUnit(
            @PathVariable Long id,
            @Valid @RequestBody CreateUnitRequest request,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                unitService.updateUnit(
                        id,
                        request,
                        authentication.getName()
                )
        );
    }

    @DeleteMapping("/api/units/{id}")
    public ResponseEntity<Void> deleteUnit(
            @PathVariable Long id,
            Authentication authentication
    ) {

        unitService.deleteUnit(
                id,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}