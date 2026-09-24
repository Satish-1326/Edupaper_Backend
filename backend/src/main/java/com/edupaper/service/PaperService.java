package com.edupaper.service;

import com.edupaper.dto.paper.GeneratePaperRequest;
import com.edupaper.dto.paper.PaperQuestionResponse;
import com.edupaper.dto.paper.PaperResponse;
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
public class PaperService {

    private final PaperRepository paperRepository;
    private final PaperQuestionRepository paperQuestionRepository;

    private final BlueprintRepository blueprintRepository;
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;

    private final QuestionSelectionService questionSelectionService;

    // =========================
    // GENERATE PAPER
    // =========================

    @Transactional
    public PaperResponse generatePaper(
            GeneratePaperRequest request,
            Long userId) {

        // 1. Find blueprint owned by current user
        Blueprint blueprint =
                blueprintRepository
                        .findByIdAndCreatedById(
                                request.getBlueprintId(),
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Blueprint not found or access denied"
                                )
                        );

        // 2. Get subject
        Subject subject = blueprint.getSubject();

        // 3. Get all questions belonging to this subject
        List<Question> availableQuestions =
                questionRepository
                        .findBySubjectIdAndCreatedById(
                                subject.getId(),
                                userId
                        );

        if (availableQuestions.isEmpty()) {
            throw new BadRequestException(
                    "No questions available for this subject"
            );
        }

        // 4. Select questions according to blueprint
        List<Question> selectedQuestions =
                questionSelectionService.selectQuestions(
                        availableQuestions,
                        blueprint
                );

        // 5. Verify required number
        if (selectedQuestions.size()
                != blueprint.getTotalQuestions()) {

            throw new BadRequestException(
                    "Unable to generate paper with the required "
                            + blueprint.getTotalQuestions()
                            + " questions. Only "
                            + selectedQuestions.size()
                            + " matching questions were found."
            );
        }

        // 6. Create Paper
        Paper paper = Paper.builder()
                .name(request.getName())
                .subject(subject)
                .blueprint(blueprint)
                .totalQuestions(
                        blueprint.getTotalQuestions()
                )
                .totalMarks(
                        blueprint.getTotalMarks()
                )
                .status(PaperStatus.GENERATED)
                .createdBy(
                        blueprint.getCreatedBy()
                )
                .build();

        // 7. Save paper first
        Paper savedPaper =
                paperRepository.save(paper);

        // 8. Create PaperQuestion records
        List<PaperQuestion> paperQuestions =
                new ArrayList<>();

        int order = 1;

        for (Question question : selectedQuestions) {

            PaperQuestion paperQuestion =
                    PaperQuestion.builder()
                            .questionOrder(order++)
                            .paper(savedPaper)
                            .question(question)
                            .build();

            paperQuestions.add(paperQuestion);
        }

        savedPaper.setQuestions(paperQuestions);

        // 9. Save paper with questions
        savedPaper =
                paperRepository.save(savedPaper);

        return mapToResponse(savedPaper);
    }

    // =========================
    // GET ALL PAPERS
    // =========================

    public List<PaperResponse> getAllPapers(
            Long userId) {

        return paperRepository
                .findByCreatedByIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =========================
    // GET PAPER
    // =========================

    public PaperResponse getPaper(
            Long paperId,
            Long userId) {

        Paper paper =
                paperRepository
                        .findByIdAndCreatedById(
                                paperId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Paper not found or access denied"
                                )
                        );

        return mapToResponse(paper);
    }

    // =========================
    // GET BY SUBJECT
    // =========================

    public List<PaperResponse> getPapersBySubject(
            Long subjectId,
            Long userId) {

        subjectRepository
                .findByIdAndCreatedById(
                        subjectId,
                        userId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Subject not found or access denied"
                        )
                );

        return paperRepository
                .findBySubjectIdAndCreatedByIdOrderByCreatedAtDesc(
                        subjectId,
                        userId
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =========================
    // DELETE
    // =========================

    @Transactional
    public void deletePaper(
            Long paperId,
            Long userId) {

        Paper paper =
                paperRepository
                        .findByIdAndCreatedById(
                                paperId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Paper not found or access denied"
                                )
                        );

        paperRepository.delete(paper);
    }

    // =========================
    // MAP RESPONSE
    // =========================

    private PaperResponse mapToResponse(
            Paper paper) {

        List<PaperQuestionResponse> questionResponses =
                paper.getQuestions()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        PaperQuestion::getQuestionOrder
                                )
                        )
                        .map(pq -> {

                            Question question =
                                    pq.getQuestion();

                            return PaperQuestionResponse
                                    .builder()
                                    .id(pq.getId())
                                    .questionOrder(
                                            pq.getQuestionOrder()
                                    )
                                    .questionId(
                                            question.getId()
                                    )
                                    .questionText(
                                            question.getQuestionText()
                                    )
                                    .questionType(
                                            question.getQuestionType()
                                                    .name()
                                    )
                                    .difficulty(
                                            question.getDifficulty()
                                                    .name()
                                    )
                                    .marks(
                                            question.getMarks()
                                    )
                                    .bloomLevel(
                                            question.getBloomLevel()
                                                    .name()
                                    )
                                    .build();
                        })
                        .collect(Collectors.toList());

        return PaperResponse.builder()
                .id(paper.getId())
                .name(paper.getName())
                .subjectId(
                        paper.getSubject().getId()
                )
                .blueprintId(
                        paper.getBlueprint().getId()
                )
                .totalQuestions(
                        paper.getTotalQuestions()
                )
                .totalMarks(
                        paper.getTotalMarks()
                )
                .status(
                        paper.getStatus()
                )
                .createdBy(
                        paper.getCreatedBy().getId()
                )
                .questions(questionResponses)
                .createdAt(
                        paper.getCreatedAt()
                )
                .updatedAt(
                        paper.getUpdatedAt()
                )
                .build();
    }
}