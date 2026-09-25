package com.edupaper.service;

import com.edupaper.dto.paper.*;
import com.edupaper.entity.*;
import com.edupaper.exception.BadRequestException;
import com.edupaper.exception.ResourceNotFoundException;
import com.edupaper.repository.*;
import jakarta.transaction.Transactional;
import java.util.Objects;
import java.util.function.Function;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaperService {


    private final PaperValidationService paperValidationService;
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

    @Transactional
    public RemovePaperQuestionResponse removeQuestion(
            Long paperId,
            Long questionId,
            User currentUser) {

        Paper paper = paperRepository
                .findByIdAndCreatedById(
                        paperId,
                        currentUser.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Paper not found"
                        )
                );

        // Only draft/generated papers can be edited
        if (paper.getStatus() == PaperStatus.FINALIZED) {
            throw new BadRequestException(
                    "Finalized paper cannot be modified"
            );
        }

        PaperQuestion paperQuestion =
                paper.getQuestions()
                        .stream()
                        .filter(pq ->
                                pq.getQuestion() != null &&
                                        pq.getQuestion()
                                                .getId()
                                                .equals(questionId)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Question not found in this paper"
                                )
                        );

        paper.getQuestions().remove(paperQuestion);

        // Reorder remaining questions
        int order = 1;

        for (PaperQuestion pq : paper.getQuestions()) {
            pq.setQuestionOrder(order++);
        }

        Paper savedPaper =
                paperRepository.save(paper);

        int remainingMarks =
                savedPaper.getQuestions()
                        .stream()
                        .map(PaperQuestion::getQuestion)
                        .filter(Objects::nonNull)
                        .map(Question::getMarks)
                        .filter(Objects::nonNull)
                        .mapToInt(Integer::intValue)
                        .sum();

        return RemovePaperQuestionResponse.builder()
                .paperId(savedPaper.getId())
                .removedQuestionId(questionId)
                .remainingQuestions(
                        savedPaper.getQuestions().size()
                )
                .remainingMarks(remainingMarks)
                .message("Question removed successfully")
                .build();
    }

    @Transactional
    public AddPaperQuestionResponse addQuestion(
            Long paperId,
            Long questionId,
            Long userId) {

        // =====================================================
        // FIND PAPER
        // =====================================================

        Paper paper = paperRepository
                .findByIdAndCreatedById(
                        paperId,
                        userId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Paper not found"
                        )
                );

        // =====================================================
        // CHECK PAPER STATUS
        // =====================================================

        if (paper.getStatus() == PaperStatus.FINALIZED) {

            throw new BadRequestException(
                    "Finalized paper cannot be modified"
            );
        }

        // =====================================================
        // FIND QUESTION
        // =====================================================

        Question question = questionRepository
                .findByIdAndCreatedById(
                        questionId,
                        userId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Question not found"
                        )
                );

        // =====================================================
        // CHECK SAME SUBJECT
        // =====================================================

        if (question.getSubject() == null ||
                paper.getSubject() == null ||
                !question.getSubject()
                        .getId()
                        .equals(
                                paper.getSubject().getId()
                        )) {

            throw new BadRequestException(
                    "Question does not belong to the same subject as the paper"
            );
        }

        // =====================================================
        // CHECK DUPLICATE
        // =====================================================

        boolean alreadyExists =
                paperQuestionRepository
                        .existsByPaperIdAndQuestionId(
                                paperId,
                                questionId
                        );

        if (alreadyExists) {

            throw new BadRequestException(
                    "Question is already present in this paper"
            );
        }

        // =====================================================
        // DETERMINE NEXT QUESTION ORDER
        // =====================================================

        int nextOrder = paper.getQuestions()
                .stream()
                .map(PaperQuestion::getQuestionOrder)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        // =====================================================
        // CREATE PAPER QUESTION
        // =====================================================

        PaperQuestion paperQuestion =
                PaperQuestion.builder()
                        .paper(paper)
                        .question(question)
                        .questionOrder(nextOrder)
                        .build();

        // =====================================================
        // ADD TO PAPER
        // =====================================================

        paper.getQuestions()
                .add(paperQuestion);

        Paper savedPaper =
                paperRepository.save(paper);

        // =====================================================
        // CALCULATE CURRENT TOTAL MARKS
        // =====================================================

        int currentTotalMarks =
                savedPaper.getQuestions()
                        .stream()
                        .map(PaperQuestion::getQuestion)
                        .filter(Objects::nonNull)
                        .map(Question::getMarks)
                        .filter(Objects::nonNull)
                        .mapToInt(Integer::intValue)
                        .sum();

        // =====================================================
        // RETURN RESPONSE
        // =====================================================

        return AddPaperQuestionResponse.builder()
                .paperId(savedPaper.getId())
                .addedQuestionId(question.getId())
                .questionOrder(nextOrder)
                .questionText(question.getQuestionText())
                .questionMarks(question.getMarks())
                .totalQuestions(
                        savedPaper.getQuestions().size()
                )
                .currentTotalMarks(currentTotalMarks)
                .message("Question added successfully")
                .build();
    }

    @Transactional
    public ReplacePaperQuestionResponse replaceQuestion(
            Long paperId,
            Long oldQuestionId,
            Long newQuestionId,
            Long userId) {

        // =====================================================
        // BASIC VALIDATION
        // =====================================================

        if (oldQuestionId == null) {
            throw new BadRequestException(
                    "Old question ID is required"
            );
        }

        if (newQuestionId == null) {
            throw new BadRequestException(
                    "New question ID is required"
            );
        }

        if (oldQuestionId.equals(newQuestionId)) {
            throw new BadRequestException(
                    "Old and new question IDs cannot be the same"
            );
        }

        // =====================================================
        // FIND PAPER
        // =====================================================

        Paper paper = paperRepository
                .findByIdAndCreatedById(
                        paperId,
                        userId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Paper not found"
                        )
                );

        // =====================================================
        // CHECK PAPER STATUS
        // =====================================================

        if (paper.getStatus() == PaperStatus.FINALIZED) {

            throw new BadRequestException(
                    "Finalized paper cannot be modified"
            );
        }

        // =====================================================
        // FIND OLD PAPER QUESTION
        // =====================================================

        PaperQuestion paperQuestion =
                paper.getQuestions()
                        .stream()
                        .filter(pq ->
                                pq.getQuestion() != null &&
                                        pq.getQuestion()
                                                .getId()
                                                .equals(oldQuestionId)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Old question not found in this paper"
                                )
                        );

        // =====================================================
        // FIND NEW QUESTION
        // =====================================================

        Question newQuestion =
                questionRepository
                        .findByIdAndCreatedById(
                                newQuestionId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "New question not found"
                                )
                        );

        // =====================================================
        // CHECK SAME SUBJECT
        // =====================================================

        if (newQuestion.getSubject() == null ||
                paper.getSubject() == null ||
                !newQuestion.getSubject()
                        .getId()
                        .equals(
                                paper.getSubject().getId()
                        )) {

            throw new BadRequestException(
                    "New question does not belong to the same subject as the paper"
            );
        }

        // =====================================================
        // CHECK DUPLICATE
        // =====================================================

        boolean alreadyExists =
                paper.getQuestions()
                        .stream()
                        .anyMatch(pq ->
                                pq.getQuestion() != null &&
                                        pq.getQuestion()
                                                .getId()
                                                .equals(newQuestionId)
                        );

        if (alreadyExists) {

            throw new BadRequestException(
                    "New question is already present in this paper"
            );
        }

        // =====================================================
        // SAVE OLD QUESTION ID
        // =====================================================

        Long replacedQuestionId =
                paperQuestion.getQuestion().getId();

        // =====================================================
        // KEEP EXISTING ORDER
        // =====================================================

        Integer existingOrder =
                paperQuestion.getQuestionOrder();

        // =====================================================
        // REPLACE QUESTION
        // =====================================================

        paperQuestion.setQuestion(newQuestion);

        // IMPORTANT:
        // Keep the existing question order.
        paperQuestion.setQuestionOrder(existingOrder);

        // =====================================================
        // SAVE PAPER
        // =====================================================

        Paper savedPaper =
                paperRepository.save(paper);

        // =====================================================
        // CALCULATE CURRENT TOTAL MARKS
        // =====================================================

        int currentTotalMarks =
                savedPaper.getQuestions()
                        .stream()
                        .map(PaperQuestion::getQuestion)
                        .filter(Objects::nonNull)
                        .map(Question::getMarks)
                        .filter(Objects::nonNull)
                        .mapToInt(Integer::intValue)
                        .sum();

        // =====================================================
        // RETURN RESPONSE
        // =====================================================

        return ReplacePaperQuestionResponse.builder()
                .paperId(savedPaper.getId())
                .oldQuestionId(replacedQuestionId)
                .newQuestionId(newQuestion.getId())
                .questionOrder(existingOrder)
                .questionText(newQuestion.getQuestionText())
                .questionType(
                        newQuestion.getQuestionType() != null
                                ? newQuestion.getQuestionType().name()
                                : null
                )
                .difficulty(
                        newQuestion.getDifficulty() != null
                                ? newQuestion.getDifficulty().name()
                                : null
                )
                .marks(newQuestion.getMarks())
                .bloomLevel(
                        newQuestion.getBloomLevel() != null
                                ? newQuestion.getBloomLevel().name()
                                : null
                )
                .totalQuestions(
                        savedPaper.getQuestions().size()
                )
                .currentTotalMarks(currentTotalMarks)
                .message("Question replaced successfully")
                .build();
    }

    @Transactional
    public ReorderPaperQuestionsResponse reorderQuestions(
            Long paperId,
            List<Long> questionIds,
            Long userId) {

        // =====================================================
        // BASIC VALIDATION
        // =====================================================

        if (questionIds == null ||
                questionIds.isEmpty()) {

            throw new BadRequestException(
                    "Question order cannot be empty"
            );
        }

        // =====================================================
        // FIND PAPER
        // =====================================================

        Paper paper = paperRepository
                .findByIdAndCreatedById(
                        paperId,
                        userId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Paper not found"
                        )
                );

        // =====================================================
        // CHECK PAPER STATUS
        // =====================================================

        if (paper.getStatus() == PaperStatus.FINALIZED) {

            throw new BadRequestException(
                    "Finalized paper cannot be modified"
            );
        }

        // =====================================================
        // REMOVE NULL VALUES
        // =====================================================

        if (questionIds.stream().anyMatch(Objects::isNull)) {

            throw new BadRequestException(
                    "Question IDs cannot contain null values"
            );
        }

        // =====================================================
        // CHECK DUPLICATE IDS IN REQUEST
        // =====================================================

        Set<Long> requestedIds =
                new LinkedHashSet<>(questionIds);

        if (requestedIds.size() != questionIds.size()) {

            throw new BadRequestException(
                    "Question order contains duplicate question IDs"
            );
        }

        // =====================================================
        // GET CURRENT PAPER QUESTIONS
        // =====================================================

        List<PaperQuestion> paperQuestions =
                paper.getQuestions();

        // =====================================================
        // CHECK SAME NUMBER OF QUESTIONS
        // =====================================================

        if (questionIds.size() !=
                paperQuestions.size()) {

            throw new BadRequestException(
                    "The reorder request must contain exactly "
                            + paperQuestions.size()
                            + " question IDs"
            );
        }

        // =====================================================
        // MAP QUESTION ID → PAPER QUESTION
        // =====================================================

        Map<Long, PaperQuestion> paperQuestionMap =
                paperQuestions.stream()
                        .filter(pq ->
                                pq.getQuestion() != null &&
                                        pq.getQuestion().getId() != null
                        )
                        .collect(
                                Collectors.toMap(
                                        pq -> pq.getQuestion().getId(),
                                        Function.identity()
                                )
                        );

        // =====================================================
        // CHECK ALL QUESTIONS BELONG TO PAPER
        // =====================================================

        for (Long questionId : questionIds) {

            if (!paperQuestionMap.containsKey(questionId)) {

                throw new BadRequestException(
                        "Question ID "
                                + questionId
                                + " does not belong to this paper"
                );
            }
        }

        // =====================================================
        // UPDATE QUESTION ORDER
        // =====================================================

        int order = 1;

        for (Long questionId : questionIds) {

            PaperQuestion paperQuestion =
                    paperQuestionMap.get(questionId);

            paperQuestion.setQuestionOrder(order++);

        }

        // =====================================================
        // SAVE
        // =====================================================

        Paper savedPaper =
                paperRepository.save(paper);

        // =====================================================
        // BUILD RESPONSE
        // =====================================================

        List<PaperQuestionOrderResponse> responseQuestions =
                savedPaper.getQuestions()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        PaperQuestion::getQuestionOrder
                                )
                        )
                        .map(pq ->
                                PaperQuestionOrderResponse.builder()
                                        .questionOrder(
                                                pq.getQuestionOrder()
                                        )
                                        .questionId(
                                                pq.getQuestion().getId()
                                        )
                                        .questionText(
                                                pq.getQuestion()
                                                        .getQuestionText()
                                        )
                                        .build()
                        )
                        .collect(Collectors.toList());

        return ReorderPaperQuestionsResponse.builder()
                .paperId(savedPaper.getId())
                .totalQuestions(
                        savedPaper.getQuestions().size()
                )
                .questions(responseQuestions)
                .message(
                        "Paper questions reordered successfully"
                )
                .build();
    }

    @Transactional
    public PaperResponse finalizePaper(
            Long paperId,
            Long userId) {

        // =====================================================
        // FIND PAPER
        // =====================================================

        Paper paper =
                paperRepository
                        .findByIdAndCreatedById(
                                paperId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Paper not found"
                                )
                        );

        // =====================================================
        // CHECK CURRENT STATUS
        // =====================================================

        if (paper.getStatus() == PaperStatus.FINALIZED) {

            throw new BadRequestException(
                    "Paper is already finalized"
            );
        }

        // =====================================================
        // VALIDATE COMPLETE PAPER
        // =====================================================

        paperValidationService.validatePaper(
                paper
        );

        // =====================================================
        // FINALIZE
        // =====================================================

        paper.setStatus(
                PaperStatus.FINALIZED
        );

        Paper savedPaper =
                paperRepository.save(paper);

        // =====================================================
        // RETURN PAPER
        // =====================================================

        return mapToResponse(savedPaper);
    }
}