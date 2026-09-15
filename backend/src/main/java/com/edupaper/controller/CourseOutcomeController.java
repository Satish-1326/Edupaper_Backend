package com.edupaper.controller;

import com.edupaper.dto.courseoutcome.CourseOutcomeResponse;
import com.edupaper.dto.courseoutcome.CreateCourseOutcomeRequest;
import com.edupaper.entity.User;
import com.edupaper.repository.UserRepository;
import com.edupaper.service.CourseOutcomeService;
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
public class CourseOutcomeController {

    private final CourseOutcomeService courseOutcomeService;
    private final UserRepository userRepository;


    // Create Course Outcome
    @PostMapping("/subjects/{subjectId}/course-outcomes")
    public ResponseEntity<CourseOutcomeResponse> createCourseOutcome(
            @PathVariable Long subjectId,
            @Valid @RequestBody CreateCourseOutcomeRequest request,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        CourseOutcomeResponse response =
                courseOutcomeService.createCourseOutcome(
                        subjectId,
                        request,
                        userId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // Get all Course Outcomes
    @GetMapping("/subjects/{subjectId}/course-outcomes")
    public ResponseEntity<List<CourseOutcomeResponse>> getCourseOutcomes(
            @PathVariable Long subjectId,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        List<CourseOutcomeResponse> response =
                courseOutcomeService.getCourseOutcomes(
                        subjectId,
                        userId
                );

        return ResponseEntity.ok(response);
    }


    // Get single Course Outcome
    @GetMapping("/course-outcomes/{coId}")
    public ResponseEntity<CourseOutcomeResponse> getCourseOutcome(
            @PathVariable Long coId,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        CourseOutcomeResponse response =
                courseOutcomeService.getCourseOutcome(
                        coId,
                        userId
                );

        return ResponseEntity.ok(response);
    }


    // Update Course Outcome
    @PutMapping("/course-outcomes/{coId}")
    public ResponseEntity<CourseOutcomeResponse> updateCourseOutcome(
            @PathVariable Long coId,
            @Valid @RequestBody CreateCourseOutcomeRequest request,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        CourseOutcomeResponse response =
                courseOutcomeService.updateCourseOutcome(
                        coId,
                        request,
                        userId
                );

        return ResponseEntity.ok(response);
    }


    // Delete Course Outcome
    @DeleteMapping("/course-outcomes/{coId}")
    public ResponseEntity<Void> deleteCourseOutcome(
            @PathVariable Long coId,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        courseOutcomeService.deleteCourseOutcome(
                coId,
                userId
        );

        return ResponseEntity.noContent().build();
    }


    // Get logged-in user's ID
    private Long getUserId(Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return user.getId();
    }
}