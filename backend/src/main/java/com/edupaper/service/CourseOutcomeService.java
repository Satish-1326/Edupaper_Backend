package com.edupaper.service;

import com.edupaper.dto.courseoutcome.CourseOutcomeResponse;
import com.edupaper.dto.courseoutcome.CreateCourseOutcomeRequest;
import com.edupaper.entity.CourseOutcome;
import com.edupaper.entity.Subject;
import com.edupaper.exception.DuplicateResourceException;
import com.edupaper.exception.ResourceNotFoundException;
import com.edupaper.repository.CourseOutcomeRepository;
import com.edupaper.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseOutcomeService {

    private final CourseOutcomeRepository courseOutcomeRepository;
    private final SubjectRepository subjectRepository;


    // Create Course Outcome
    @Transactional
    public CourseOutcomeResponse createCourseOutcome(
            Long subjectId,
            CreateCourseOutcomeRequest request,
            Long userId
    ) {

        // Check subject ownership
        Subject subject = subjectRepository
                .findByIdAndCreatedById(subjectId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Subject not found or you do not have access to this subject"
                        )
                );

        // Prevent duplicate CO number
        if (courseOutcomeRepository.existsBySubjectIdAndCoNumber(
                subjectId,
                request.getCoNumber()
        )) {

            throw new DuplicateResourceException(
                    "CO" + request.getCoNumber()
                            + " already exists for this subject"
            );
        }

        CourseOutcome courseOutcome = CourseOutcome.builder()
                .coNumber(request.getCoNumber())
                .description(request.getDescription())
                .subject(subject)
                .build();

        CourseOutcome savedCourseOutcome =
                courseOutcomeRepository.save(courseOutcome);

        return mapToResponse(savedCourseOutcome);
    }


    // Get all Course Outcomes of a Subject
    @Transactional(readOnly = true)
    public List<CourseOutcomeResponse> getCourseOutcomes(
            Long subjectId,
            Long userId
    ) {

        // Check subject ownership
        subjectRepository
                .findByIdAndCreatedById(subjectId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Subject not found or you do not have access to this subject"
                        )
                );

        return courseOutcomeRepository
                .findBySubjectIdOrderByCoNumberAsc(subjectId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // Get single Course Outcome
    @Transactional(readOnly = true)
    public CourseOutcomeResponse getCourseOutcome(
            Long coId,
            Long userId
    ) {

        CourseOutcome courseOutcome =
                courseOutcomeRepository
                        .findByIdAndSubjectCreatedById(coId, userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Course Outcome not found or you do not have access to this CO"
                                )
                        );

        return mapToResponse(courseOutcome);
    }


    // Update Course Outcome
    @Transactional
    public CourseOutcomeResponse updateCourseOutcome(
            Long coId,
            CreateCourseOutcomeRequest request,
            Long userId
    ) {

        CourseOutcome courseOutcome =
                courseOutcomeRepository
                        .findByIdAndSubjectCreatedById(coId, userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Course Outcome not found or you do not have access to this CO"
                                )
                        );

        // Check duplicate CO number if changed
        if (!courseOutcome.getCoNumber().equals(request.getCoNumber())
                && courseOutcomeRepository
                .existsBySubjectIdAndCoNumberAndIdNot(
                        courseOutcome.getSubject().getId(),
                        request.getCoNumber(),
                        coId
                )) {

            throw new DuplicateResourceException(
                    "CO" + request.getCoNumber()
                            + " already exists for this subject"
            );
        }

        courseOutcome.setCoNumber(request.getCoNumber());
        courseOutcome.setDescription(request.getDescription());

        CourseOutcome updatedCourseOutcome =
                courseOutcomeRepository.save(courseOutcome);

        return mapToResponse(updatedCourseOutcome);
    }


    // Delete Course Outcome
    @Transactional
    public void deleteCourseOutcome(
            Long coId,
            Long userId
    ) {

        CourseOutcome courseOutcome =
                courseOutcomeRepository
                        .findByIdAndSubjectCreatedById(coId, userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Course Outcome not found or you do not have access to this CO"
                                )
                        );

        courseOutcomeRepository.delete(courseOutcome);
    }


    // Entity → Response DTO
    private CourseOutcomeResponse mapToResponse(
            CourseOutcome courseOutcome
    ) {

        return CourseOutcomeResponse.builder()
                .id(courseOutcome.getId())
                .coNumber(courseOutcome.getCoNumber())
                .description(courseOutcome.getDescription())
                .subjectId(courseOutcome.getSubject().getId())
                .build();
    }
}