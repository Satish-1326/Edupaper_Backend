package com.edupaper.service;

import com.edupaper.dto.topic.CreateTopicRequest;
import com.edupaper.dto.topic.TopicResponse;
import com.edupaper.entity.Topic;
import com.edupaper.entity.Unit;
import com.edupaper.exception.DuplicateResourceException;
import com.edupaper.exception.ResourceNotFoundException;
import com.edupaper.repository.TopicRepository;
import com.edupaper.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final UnitRepository unitRepository;

    // Create Topic
    @Transactional
    public TopicResponse createTopic(
            Long unitId,
            CreateTopicRequest request,
            Long userId
    ) {

        // Check whether unit exists and belongs to logged-in user
        Unit unit = unitRepository
                .findByIdAndSubjectCreatedById(unitId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Unit not found or you do not have access to this unit"
                        )
                );

        // Prevent duplicate topic name inside same unit
        if (topicRepository.existsByUnitIdAndName(
                unitId,
                request.getName()
        )) {
            throw new DuplicateResourceException(
                    "Topic with this name already exists in this unit"
            );
        }

        Topic topic = Topic.builder()
                .name(request.getName())
                .description(request.getDescription())
                .unit(unit)
                .build();

        Topic savedTopic = topicRepository.save(topic);

        return mapToResponse(savedTopic);
    }


    // Get all Topics of a Unit
    @Transactional(readOnly = true)
    public List<TopicResponse> getTopicsByUnit(
            Long unitId,
            Long userId
    ) {

        // First verify unit ownership
        unitRepository
                .findByIdAndSubjectCreatedById(unitId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Unit not found or you do not have access to this unit"
                        )
                );

        return topicRepository
                .findByUnitIdOrderByNameAsc(unitId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // Get single Topic
    @Transactional(readOnly = true)
    public TopicResponse getTopic(
            Long topicId,
            Long userId
    ) {

        Topic topic = topicRepository
                .findByIdAndUnitSubjectCreatedById(topicId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Topic not found or you do not have access to this topic"
                        )
                );

        return mapToResponse(topic);
    }


    // Update Topic
    @Transactional
    public TopicResponse updateTopic(
            Long topicId,
            CreateTopicRequest request,
            Long userId
    ) {

        Topic topic = topicRepository
                .findByIdAndUnitSubjectCreatedById(topicId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Topic not found or you do not have access to this topic"
                        )
                );

        // Check duplicate name only if name is changed
        if (!topic.getName().equals(request.getName())
                && topicRepository.existsByUnitIdAndNameAndIdNot(
                topic.getUnit().getId(),
                request.getName(),
                topicId
        )) {

            throw new DuplicateResourceException(
                    "Topic with this name already exists in this unit"
            );
        }

        topic.setName(request.getName());
        topic.setDescription(request.getDescription());

        Topic updatedTopic = topicRepository.save(topic);

        return mapToResponse(updatedTopic);
    }


    // Delete Topic
    @Transactional
    public void deleteTopic(
            Long topicId,
            Long userId
    ) {

        Topic topic = topicRepository
                .findByIdAndUnitSubjectCreatedById(topicId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Topic not found or you do not have access to this topic"
                        )
                );

        topicRepository.delete(topic);
    }


    // Convert Entity → DTO
    private TopicResponse mapToResponse(Topic topic) {

        return TopicResponse.builder()
                .id(topic.getId())
                .name(topic.getName())
                .description(topic.getDescription())
                .unitId(topic.getUnit().getId())
                .build();
    }
}