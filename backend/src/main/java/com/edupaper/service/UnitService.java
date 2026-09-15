package com.edupaper.service;

import com.edupaper.dto.unit.CreateUnitRequest;
import com.edupaper.dto.unit.UnitResponse;
import com.edupaper.entity.Subject;
import com.edupaper.entity.Unit;
import com.edupaper.entity.User;
import com.edupaper.exception.DuplicateResourceException;
import com.edupaper.exception.ResourceNotFoundException;
import com.edupaper.repository.SubjectRepository;
import com.edupaper.repository.UnitRepository;
import com.edupaper.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UnitService {

    private final UnitRepository unitRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public UnitResponse createUnit(
            Long subjectId,
            CreateUnitRequest request,
            String email
    ) {

        User user = getUser(email);

        Subject subject = subjectRepository
                .findByIdAndCreatedById(
                        subjectId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Subject not found"
                        )
                );

        if (unitRepository.existsBySubjectIdAndUnitNumber(
                subjectId,
                request.getUnitNumber()
        )) {
            throw new DuplicateResourceException(
                    "Unit number already exists for this subject"
            );
        }

        Unit unit = Unit.builder()
                .unitNumber(request.getUnitNumber())
                .title(request.getTitle())
                .description(request.getDescription())
                .subject(subject)
                .build();

        Unit savedUnit = unitRepository.save(unit);

        return mapToResponse(savedUnit);
    }

    @Transactional(readOnly = true)
    public List<UnitResponse> getUnitsBySubject(
            Long subjectId,
            String email
    ) {

        User user = getUser(email);

        // Verify ownership of subject
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

        return unitRepository
                .findBySubjectIdOrderByUnitNumberAsc(subjectId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UnitResponse getUnit(
            Long unitId,
            String email
    ) {

        User user = getUser(email);

        Unit unit = unitRepository
                .findByIdAndSubjectCreatedById(
                        unitId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Unit not found"
                        )
                );

        return mapToResponse(unit);
    }

    public UnitResponse updateUnit(
            Long unitId,
            CreateUnitRequest request,
            String email
    ) {

        User user = getUser(email);

        Unit unit = unitRepository
                .findByIdAndSubjectCreatedById(
                        unitId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Unit not found"
                        )
                );

        Long subjectId = unit.getSubject().getId();

        if (unitRepository.existsBySubjectIdAndUnitNumberAndIdNot(
                subjectId,
                request.getUnitNumber(),
                unitId
        )) {
            throw new DuplicateResourceException(
                    "Unit number already exists for this subject"
            );
        }

        unit.setUnitNumber(request.getUnitNumber());
        unit.setTitle(request.getTitle());
        unit.setDescription(request.getDescription());

        Unit updatedUnit = unitRepository.save(unit);

        return mapToResponse(updatedUnit);
    }

    public void deleteUnit(
            Long unitId,
            String email
    ) {

        User user = getUser(email);

        Unit unit = unitRepository
                .findByIdAndSubjectCreatedById(
                        unitId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Unit not found"
                        )
                );

        unitRepository.delete(unit);
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

    private UnitResponse mapToResponse(Unit unit) {

        return UnitResponse.builder()
                .id(unit.getId())
                .unitNumber(unit.getUnitNumber())
                .title(unit.getTitle())
                .description(unit.getDescription())
                .subjectId(unit.getSubject().getId())
                .build();
    }
}