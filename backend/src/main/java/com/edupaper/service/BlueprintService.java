package com.edupaper.service;

import com.edupaper.dto.blueprint.*;
import com.edupaper.entity.*;
import com.edupaper.exception.BadRequestException;
import com.edupaper.exception.ResourceNotFoundException;
import com.edupaper.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlueprintService {

    private final BlueprintRepository blueprintRepository;
    private final SubjectRepository subjectRepository;
    private final UnitRepository unitRepository;
    private final CourseOutcomeRepository courseOutcomeRepository;

    // =========================
    // CREATE BLUEPRINT
    // =========================

    @Transactional
    public BlueprintResponse createBlueprint(
            Long subjectId,
            CreateBlueprintRequest request,
            Long userId) {

        // Validate subject ownership
        Subject subject = subjectRepository
                .findByIdAndCreatedById(subjectId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Subject not found or access denied"));

        // Validate blueprint constraints
        validateConstraints(
                subjectId,
                request.getConstraints(),
                request.getTotalQuestions(),
                userId
        );

        Blueprint blueprint = Blueprint.builder()
                .name(request.getName())
                .description(request.getDescription())
                .totalQuestions(request.getTotalQuestions())
                .totalMarks(request.getTotalMarks())
                .subject(subject)
                .createdBy(subject.getCreatedBy())
                .build();

        List<BlueprintConstraint> constraintEntities =
                new ArrayList<>();

        if (request.getConstraints() != null) {

            for (BlueprintConstraintRequest constraintRequest
                    : request.getConstraints()) {

                BlueprintConstraint constraint =
                        BlueprintConstraint.builder()
                                .constraintType(
                                        constraintRequest.getConstraintType())
                                .value(constraintRequest.getValue())
                                .requiredCount(
                                        constraintRequest.getRequiredCount())
                                .blueprint(blueprint)
                                .build();

                constraintEntities.add(constraint);
            }
        }

        blueprint.setConstraint(constraintEntities);

        Blueprint savedBlueprint =
                blueprintRepository.save(blueprint);

        return mapToResponse(savedBlueprint);
    }

    // =========================
    // GET ALL BLUEPRINTS
    // =========================

    public List<BlueprintResponse> getAllBlueprints(Long userId) {

        return blueprintRepository
                .findByCreatedByIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =========================
    // GET BLUEPRINT BY ID
    // =========================

    public BlueprintResponse getBlueprint(
            Long blueprintId,
            Long userId) {

        Blueprint blueprint =
                blueprintRepository
                        .findByIdAndCreatedById(
                                blueprintId,
                                userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Blueprint not found or access denied"));

        return mapToResponse(blueprint);
    }

    // =========================
    // GET BLUEPRINTS BY SUBJECT
    // =========================

    public List<BlueprintResponse> getBlueprintsBySubject(
            Long subjectId,
            Long userId) {

        // Verify ownership
        subjectRepository
                .findByIdAndCreatedById(subjectId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Subject not found or access denied"));

        return blueprintRepository
                .findBySubjectIdAndCreatedByIdOrderByCreatedAtDesc(
                        subjectId,
                        userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =========================
    // UPDATE BLUEPRINT
    // =========================

    @Transactional
    public BlueprintResponse updateBlueprint(
            Long blueprintId,
            CreateBlueprintRequest request,
            Long userId) {

        Blueprint blueprint =
                blueprintRepository
                        .findByIdAndCreatedById(
                                blueprintId,
                                userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Blueprint not found or access denied"));

        Long subjectId =
                blueprint.getSubject().getId();

        validateConstraints(
                subjectId,
                request.getConstraints(),
                request.getTotalQuestions(),
                userId
        );

        blueprint.setName(request.getName());
        blueprint.setDescription(request.getDescription());
        blueprint.setTotalQuestions(
                request.getTotalQuestions());
        blueprint.setTotalMarks(
                request.getTotalMarks());

        // Remove old constraints
        blueprint.getConstraint().clear();

        // Add new constraints
        if (request.getConstraints() != null) {

            for (BlueprintConstraintRequest constraintRequest
                    : request.getConstraints()) {

                BlueprintConstraint constraint =
                        BlueprintConstraint.builder()
                                .constraintType(
                                        constraintRequest.getConstraintType())
                                .value(
                                        constraintRequest.getValue())
                                .requiredCount(
                                        constraintRequest.getRequiredCount())
                                .blueprint(blueprint)
                                .build();

                blueprint.getConstraint().add(constraint);
            }
        }

        Blueprint updatedBlueprint =
                blueprintRepository.save(blueprint);

        return mapToResponse(updatedBlueprint);
    }

    // =========================
    // DELETE BLUEPRINT
    // =========================

    @Transactional
    public void deleteBlueprint(
            Long blueprintId,
            Long userId) {

        Blueprint blueprint =
                blueprintRepository
                        .findByIdAndCreatedById(
                                blueprintId,
                                userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Blueprint not found or access denied"));

        blueprintRepository.delete(blueprint);
    }

    // =========================
    // VALIDATE CONSTRAINTS
    // =========================

    private void validateConstraints(
            Long subjectId,
            List<BlueprintConstraintRequest> constraints,
            Integer totalQuestions,
            Long userId) {

        if (constraints == null || constraints.isEmpty()) {
            return;
        }

        Set<String> uniqueConstraints = new HashSet<>();

        Map<BlueprintConstraintType, Integer> countByType =
                new EnumMap<>(BlueprintConstraintType.class);

        for (BlueprintConstraintRequest request : constraints) {

            if (request.getConstraintType() == null) {
                throw new BadRequestException(
                        "Constraint type is required");
            }

            if (request.getValue() == null ||
                    request.getValue().isBlank()) {

                throw new BadRequestException(
                        "Constraint value is required");
            }

            if (request.getRequiredCount() == null ||
                    request.getRequiredCount() < 1) {

                throw new BadRequestException(
                        "Required count must be at least 1");
            }

            // Prevent duplicate type + value
            String uniqueKey =
                    request.getConstraintType().name()
                            + ":"
                            + request.getValue();

            if (!uniqueConstraints.add(uniqueKey)) {

                throw new BadRequestException(
                        "Duplicate constraint: " + uniqueKey);
            }

            // Validate value based on constraint type
            validateConstraintValue(
                    subjectId,
                    request,
                    userId
            );

            countByType.merge(
                    request.getConstraintType(),
                    request.getRequiredCount(),
                    Integer::sum
            );
        }

        // Each constraint category must represent
        // the complete question distribution.
        for (Map.Entry<BlueprintConstraintType, Integer> entry
                : countByType.entrySet()) {

            if (!entry.getValue().equals(totalQuestions)) {

                throw new BadRequestException(
                        "Total required count for "
                                + entry.getKey()
                                + " must equal totalQuestions ("
                                + totalQuestions
                                + "). Current value: "
                                + entry.getValue());
            }
        }
    }

    // =========================
    // VALIDATE CONSTRAINT VALUE
    // =========================

    private void validateConstraintValue(
            Long subjectId,
            BlueprintConstraintRequest request,
            Long userId) {

        BlueprintConstraintType type =
                request.getConstraintType();

        String value = request.getValue();

        switch (type) {

            case UNIT:

                validateUnitValue(
                        subjectId,
                        value,
                        userId
                );

                break;

            case COURSE_OUTCOME:

                validateCourseOutcomeValue(
                        subjectId,
                        value,
                        userId
                );

                break;

            case DIFFICULTY:

                try {

                    Difficulty.valueOf(
                            value.toUpperCase());

                } catch (IllegalArgumentException e) {

                    throw new BadRequestException(
                            "Invalid difficulty: " + value
                                    + ". Allowed values: EASY, MEDIUM, HARD");
                }

                break;

            case BLOOM_LEVEL:

                try {

                    BloomLevel.valueOf(
                            value.toUpperCase());

                } catch (IllegalArgumentException e) {

                    throw new BadRequestException(
                            "Invalid Bloom level: " + value
                                    + ". Allowed values: "
                                    + "L1_REMEMBER, "
                                    + "L2_UNDERSTAND, "
                                    + "L3_APPLY, "
                                    + "L4_ANALYZE, "
                                    + "L5_EVALUATE, "
                                    + "L6_CREATE");
                }

                break;

            case QUESTION_TYPE:

                try {

                    QuestionType.valueOf(
                            value.toUpperCase());

                } catch (IllegalArgumentException e) {

                    throw new BadRequestException(
                            "Invalid question type: " + value);
                }

                break;

            default:

                throw new BadRequestException(
                        "Unsupported blueprint constraint type");
        }
    }

    // =========================
    // VALIDATE UNIT
    // =========================

    private void validateUnitValue(
            Long subjectId,
            String value,
            Long userId) {

        Long unitId;

        try {

            unitId = Long.parseLong(value);

        } catch (NumberFormatException e) {

            throw new BadRequestException(
                    "UNIT constraint value must be a valid Unit ID");
        }

        unitRepository
                .findByIdAndSubjectCreatedById(
                        unitId,
                        userId)
                .orElseThrow(() ->
                        new BadRequestException(
                                "Unit does not exist or access denied"));

        Unit unit = unitRepository
                .findById(unitId)
                .orElseThrow(() ->
                        new BadRequestException(
                                "Unit not found"));

        if (!unit.getSubject().getId().equals(subjectId)) {

            throw new BadRequestException(
                    "Unit does not belong to this subject");
        }
    }

    // =========================
    // VALIDATE COURSE OUTCOME
    // =========================

    private void validateCourseOutcomeValue(
            Long subjectId,
            String value,
            Long userId) {

        Long coId;

        try {

            coId = Long.parseLong(value);

        } catch (NumberFormatException e) {

            throw new BadRequestException(
                    "COURSE_OUTCOME constraint value "
                            + "must be a valid Course Outcome ID");
        }

        CourseOutcome courseOutcome =
                courseOutcomeRepository
                        .findByIdAndSubjectCreatedById(
                                coId,
                                userId)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Course Outcome does not exist "
                                                + "or access denied"));

        if (!courseOutcome.getSubject().getId().equals(subjectId)) {

            throw new BadRequestException(
                    "Course Outcome does not belong to this subject");
        }
    }

    // =========================
    // MAP ENTITY → RESPONSE
    // =========================

    private BlueprintResponse mapToResponse(
            Blueprint blueprint) {

        List<BlueprintConstraintResponse> constraints =
                blueprint.getConstraint()
                        .stream()
                        .map(constraint ->
                                BlueprintConstraintResponse.builder()
                                        .id(constraint.getId())
                                        .constraintType(
                                                constraint.getConstraintType())
                                        .value(
                                                constraint.getValue())
                                        .requiredCount(
                                                constraint.getRequiredCount())
                                        .build()
                        )
                        .collect(Collectors.toList());

        return BlueprintResponse.builder()
                .id(blueprint.getId())
                .name(blueprint.getName())
                .description(blueprint.getDescription())
                .totalQuestions(
                        blueprint.getTotalQuestions())
                .totalMarks(
                        blueprint.getTotalMarks())
                .subjectId(
                        blueprint.getSubject().getId())
                .createdBy(
                        blueprint.getCreatedBy().getId())
                .constraints(constraints)
                .createdAt(
                        blueprint.getCreatedAt())
                .updatedAt(
                        blueprint.getUpdatedAt())
                .build();
    }
}