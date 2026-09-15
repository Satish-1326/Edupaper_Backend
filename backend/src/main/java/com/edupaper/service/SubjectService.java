package com.edupaper.service;

import com.edupaper.dto.subject.CreateSubjectRequest;
import com.edupaper.dto.subject.SubjectResponse;
import com.edupaper.entity.Subject;
import com.edupaper.entity.User;
import com.edupaper.exception.DuplicateResourceException;
import com.edupaper.exception.ResourceNotFoundException;
import com.edupaper.repository.SubjectRepository;
import com.edupaper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public SubjectResponse createSubject(
            CreateSubjectRequest request,
            String email
    ) {

        User user = getUser(email);

        if (subjectRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException(
                    "Subject code already exists"
            );
        }

        Subject subject = Subject.builder()
                .name(request.getName())
                .code(request.getCode())
                .department(request.getDepartment())
                .semester(request.getSemester())
                .academicYear(request.getAcademicYear())
                .credits(request.getCredits())
                .description(request.getDescription())
                .createdBy(user)
                .build();

        Subject savedSubject =
                subjectRepository.save(subject);

        return mapToResponse(savedSubject);
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getMySubjects(
            String email
    ) {

        User user = getUser(email);

        return subjectRepository
                .findByCreatedById(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubjectResponse getSubject(
            Long subjectId,
            String email
    ) {

        User user = getUser(email);

        Subject subject =
                subjectRepository
                        .findByIdAndCreatedById(
                                subjectId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Subject not found"
                                )
                        );

        return mapToResponse(subject);
    }

    public SubjectResponse updateSubject(
            Long subjectId,
            CreateSubjectRequest request,
            String email
    ) {

        User user = getUser(email);

        Subject subject =
                subjectRepository
                        .findByIdAndCreatedById(
                                subjectId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Subject not found"
                                )
                        );

        if (subjectRepository.existsByCodeAndIdNot(
                request.getCode(),
                subjectId
        )) {

            throw new DuplicateResourceException(
                    "Subject code already exists"
            );
        }

        subject.setName(request.getName());
        subject.setCode(request.getCode());
        subject.setDepartment(request.getDepartment());
        subject.setSemester(request.getSemester());
        subject.setAcademicYear(request.getAcademicYear());
        subject.setCredits(request.getCredits());
        subject.setDescription(request.getDescription());

        Subject updated =
                subjectRepository.save(subject);

        return mapToResponse(updated);
    }

    public void deleteSubject(
            Long subjectId,
            String email
    ) {

        User user = getUser(email);

        Subject subject =
                subjectRepository
                        .findByIdAndCreatedById(
                                subjectId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Subject not found"
                                )
                        );

        subjectRepository.delete(subject);
    }

    private User getUser(String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    private SubjectResponse mapToResponse(
            Subject subject
    ) {

        return SubjectResponse.builder()
                .id(subject.getId())
                .name(subject.getName())
                .code(subject.getCode())
                .department(subject.getDepartment())
                .semester(subject.getSemester())
                .academicYear(subject.getAcademicYear())
                .credits(subject.getCredits())
                .description(subject.getDescription())
                .createdBy(subject.getCreatedBy().getId())
                .build();
    }
}