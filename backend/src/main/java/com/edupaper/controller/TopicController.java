package com.edupaper.controller;

import com.edupaper.dto.topic.CreateTopicRequest;
import com.edupaper.dto.topic.TopicResponse;
import com.edupaper.entity.User;
import com.edupaper.repository.UserRepository;
import com.edupaper.service.TopicService;
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
public class TopicController {

    private final TopicService topicService;
    private final UserRepository userRepository;


    // Create Topic
    @PostMapping("/units/{unitId}/topics")
    public ResponseEntity<TopicResponse> createTopic(
            @PathVariable Long unitId,
            @Valid @RequestBody CreateTopicRequest request,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        TopicResponse response =
                topicService.createTopic(unitId, request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // Get all Topics of a Unit
    @GetMapping("/units/{unitId}/topics")
    public ResponseEntity<List<TopicResponse>> getTopics(
            @PathVariable Long unitId,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        List<TopicResponse> response =
                topicService.getTopicsByUnit(unitId, userId);

        return ResponseEntity.ok(response);
    }


    // Get single Topic
    @GetMapping("/topics/{topicId}")
    public ResponseEntity<TopicResponse> getTopic(
            @PathVariable Long topicId,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        TopicResponse response =
                topicService.getTopic(topicId, userId);

        return ResponseEntity.ok(response);
    }


    // Update Topic
    @PutMapping("/topics/{topicId}")
    public ResponseEntity<TopicResponse> updateTopic(
            @PathVariable Long topicId,
            @Valid @RequestBody CreateTopicRequest request,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        TopicResponse response =
                topicService.updateTopic(topicId, request, userId);

        return ResponseEntity.ok(response);
    }


    // Delete Topic
    @DeleteMapping("/topics/{topicId}")
    public ResponseEntity<Void> deleteTopic(
            @PathVariable Long topicId,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        topicService.deleteTopic(topicId, userId);

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