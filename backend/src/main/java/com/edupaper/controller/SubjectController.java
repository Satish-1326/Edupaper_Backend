package com.edupaper.controller;

import com.edupaper.dto.subject.CreateSubjectRequest;
import com.edupaper.dto.subject.SubjectResponse;
import com.edupaper.service.SubjectService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    public ResponseEntity<SubjectResponse> createSubject(
            @Valid @RequestBody CreateSubjectRequest request,
            Authentication authentication
    ) {

        SubjectResponse response =
                subjectService.createSubject(
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<SubjectResponse>> getMySubjects(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                subjectService.getMySubjects(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponse> getSubject(
            @PathVariable Long id,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                subjectService.getSubject(
                        id,
                        authentication.getName()
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubjectResponse> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody CreateSubjectRequest request,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                subjectService.updateSubject(
                        id,
                        request,
                        authentication.getName()
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubject(
            @PathVariable Long id,
            Authentication authentication
    ) {

        subjectService.deleteSubject(
                id,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}