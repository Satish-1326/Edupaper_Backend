package com.edupaper.service;

import com.edupaper.dto.question.CreateQuestionRequest;
import com.edupaper.dto.question.QuestionOptionRequest;
import com.edupaper.dto.question.QuestionOptionResponse;
import com.edupaper.dto.question.QuestionResponse;
import com.edupaper.entity.*;
import com.edupaper.exception.ResourceNotFoundException;
import com.edupaper.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final UnitRepository unitRepository;
    private final TopicRepository topicRepository;
    private final CourseOutcomeRepository courseOutcomeRepository;
    private final UserRepository userRepository;


    // =========================================================
    // CREATE QUESTION
    // =========================================================

    @Transactional
    public QuestionResponse createQuestion(
            CreateQuestionRequest request,
            Long userId
    ) {

        // -----------------------------------------------------
        // 1. Find Unit and verify ownership
        // -----------------------------------------------------

        Unit unit = unitRepository
                .findByIdAndSubjectCreatedById(
                        request.getUnitId(),
                        userId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Unit not found or you do not have access to this unit"
                        )
                );

        Subject subject = unit.getSubject();


        // -----------------------------------------------------
        // 2. Validate Topic
        // -----------------------------------------------------

        Topic topic = null;

        if (request.getTopicId() != null) {

            topic = topicRepository
                    .findByIdAndUnitIdAndUnitSubjectCreatedById(
                            request.getTopicId(),
                            unit.getId(),
                            userId
                    )
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Topic not found, does not belong to this unit, or you do not have access"
                            )
                    );
        }


        // -----------------------------------------------------
        // 3. Validate Course Outcome
        // -----------------------------------------------------

        CourseOutcome courseOutcome = null;

        if (request.getCourseOutcomeId() != null) {

            courseOutcome = courseOutcomeRepository
                    .findByIdAndSubjectIdAndSubjectCreatedById(
                            request.getCourseOutcomeId(),
                            subject.getId(),
                            userId
                    )
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Course Outcome not found, does not belong to this subject, or you do not have access"
                            )
                    );
        }


        // -----------------------------------------------------
        // 4. Find logged-in user
        // -----------------------------------------------------

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );


        // -----------------------------------------------------
        // 5. Validate question-specific rules
        // -----------------------------------------------------

        validateQuestion(request);


        // -----------------------------------------------------
        // 6. Create Question entity
        // -----------------------------------------------------

        Question question = Question.builder()
                .questionText(request.getQuestionText())
                .answer(request.getAnswer())
                .explanation(request.getExplanation())
                .questionType(request.getQuestionType())
                .difficulty(request.getDifficulty())
                .marks(request.getMarks())
                .bloomLevel(request.getBloomLevel())
                .subject(subject)
                .unit(unit)
                .topic(topic)
                .courseOutcome(courseOutcome)
                .source(request.getSource())
                .tags(request.getTags())
                .createdBy(user)
                .build();


        // -----------------------------------------------------
        // 7. Add options
        // -----------------------------------------------------

        if (request.getOptions() != null) {

            for (QuestionOptionRequest optionRequest :
                    request.getOptions()) {

                QuestionOption option = QuestionOption.builder()
                        .optionLabel(optionRequest.getOptionLabel())
                        .optionText(optionRequest.getOptionText())
                        .correct(
                                optionRequest.getCorrect() != null
                                        ? optionRequest.getCorrect()
                                        : false
                        )
                        .question(question)
                        .build();

                question.getOptions().add(option);
            }
        }


        // -----------------------------------------------------
        // 8. Save
        // -----------------------------------------------------

        Question savedQuestion =
                questionRepository.save(question);

        return mapToResponse(savedQuestion);
    }


    // =========================================================
    // GET ALL QUESTIONS
    // =========================================================

    @Transactional(readOnly = true)
    public List<QuestionResponse> getAllQuestions(
            Long userId
    ) {

        return questionRepository
                .findByCreatedByIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // GET ONE QUESTION
    // =========================================================

    @Transactional(readOnly = true)
    public QuestionResponse getQuestion(
            Long questionId,
            Long userId
    ) {

        Question question =
                questionRepository
                        .findByIdAndCreatedById(
                                questionId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Question not found or you do not have access to this question"
                                )
                        );

        return mapToResponse(question);
    }


    // =========================================================
    // GET QUESTIONS BY SUBJECT
    // =========================================================

    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsBySubject(
            Long subjectId,
            Long userId
    ) {

        subjectRepository
                .findByIdAndCreatedById(
                        subjectId,
                        userId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Subject not found or you do not have access"
                        )
                );

        return questionRepository
                .findBySubjectIdAndCreatedById(
                        subjectId,
                        userId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // GET QUESTIONS BY UNIT
    // =========================================================

    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsByUnit(
            Long unitId,
            Long userId
    ) {

        unitRepository
                .findByIdAndSubjectCreatedById(
                        unitId,
                        userId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Unit not found or you do not have access"
                        )
                );

        return questionRepository
                .findByUnitIdAndCreatedById(
                        unitId,
                        userId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // GET QUESTIONS BY TOPIC
    // =========================================================

    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsByTopic(
            Long topicId,
            Long userId
    ) {

        topicRepository
                .findByIdAndUnitSubjectCreatedById(
                        topicId,
                        userId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Topic not found or you do not have access"
                        )
                );

        return questionRepository
                .findByTopicIdAndCreatedById(
                        topicId,
                        userId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // GET QUESTIONS BY CO
    // =========================================================

    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsByCourseOutcome(
            Long courseOutcomeId,
            Long userId
    ) {

        courseOutcomeRepository
                .findByIdAndSubjectCreatedById(
                        courseOutcomeId,
                        userId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Course Outcome not found or you do not have access"
                        )
                );

        return questionRepository
                .findByCourseOutcomeIdAndCreatedById(
                        courseOutcomeId,
                        userId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // UPDATE QUESTION
    // =========================================================

    @Transactional
    public QuestionResponse updateQuestion(
            Long questionId,
            CreateQuestionRequest request,
            Long userId
    ) {

        Question question =
                questionRepository
                        .findByIdAndCreatedById(
                                questionId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Question not found or you do not have access"
                                )
                        );


        // Validate new unit
        Unit unit = unitRepository
                .findByIdAndSubjectCreatedById(
                        request.getUnitId(),
                        userId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Unit not found or you do not have access"
                        )
                );


        Subject subject = unit.getSubject();


        // Validate topic
        Topic topic = null;

        if (request.getTopicId() != null) {

            topic = topicRepository
                    .findByIdAndUnitIdAndUnitSubjectCreatedById(
                            request.getTopicId(),
                            unit.getId(),
                            userId
                    )
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Topic does not belong to this unit"
                            )
                    );
        }


        // Validate CO
        CourseOutcome courseOutcome = null;

        if (request.getCourseOutcomeId() != null) {

            courseOutcome = courseOutcomeRepository
                    .findByIdAndSubjectIdAndSubjectCreatedById(
                            request.getCourseOutcomeId(),
                            subject.getId(),
                            userId
                    )
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Course Outcome does not belong to this subject"
                            )
                    );
        }


        validateQuestion(request);


        // Update fields
        question.setQuestionText(request.getQuestionText());
        question.setAnswer(request.getAnswer());
        question.setExplanation(request.getExplanation());
        question.setQuestionType(request.getQuestionType());
        question.setDifficulty(request.getDifficulty());
        question.setMarks(request.getMarks());
        question.setBloomLevel(request.getBloomLevel());
        question.setSubject(subject);
        question.setUnit(unit);
        question.setTopic(topic);
        question.setCourseOutcome(courseOutcome);
        question.setSource(request.getSource());
        question.setTags(request.getTags());


        // Replace old options
        question.getOptions().clear();

        if (request.getOptions() != null) {

            for (QuestionOptionRequest optionRequest :
                    request.getOptions()) {

                QuestionOption option = QuestionOption.builder()
                        .optionLabel(optionRequest.getOptionLabel())
                        .optionText(optionRequest.getOptionText())
                        .correct(
                                optionRequest.getCorrect() != null
                                        ? optionRequest.getCorrect()
                                        : false
                        )
                        .question(question)
                        .build();

                question.getOptions().add(option);
            }
        }


        Question updatedQuestion =
                questionRepository.save(question);

        return mapToResponse(updatedQuestion);
    }


    // =========================================================
    // DELETE QUESTION
    // =========================================================

    @Transactional
    public void deleteQuestion(
            Long questionId,
            Long userId
    ) {

        Question question =
                questionRepository
                        .findByIdAndCreatedById(
                                questionId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Question not found or you do not have access"
                                )
                        );

        questionRepository.delete(question);
    }


    // =========================================================
    // QUESTION VALIDATION
    // =========================================================

    private void validateQuestion(
            CreateQuestionRequest request
    ) {

        QuestionType type = request.getQuestionType();

        List<QuestionOptionRequest> options =
                request.getOptions();


        // -----------------------------------------------------
        // MCQ
        // -----------------------------------------------------

        if (type == QuestionType.MCQ) {

            if (options == null || options.size() < 2) {

                throw new IllegalArgumentException(
                        "MCQ must contain at least 2 options"
                );
            }

            long correctCount = options
                    .stream()
                    .filter(option ->
                            Boolean.TRUE.equals(option.getCorrect()))
                    .count();

            if (correctCount != 1) {

                throw new IllegalArgumentException(
                        "MCQ must have exactly one correct option"
                );
            }
        }


        // -----------------------------------------------------
        // MSQ
        // -----------------------------------------------------

        if (type == QuestionType.MSQ) {

            if (options == null || options.size() < 2) {

                throw new IllegalArgumentException(
                        "MSQ must contain at least 2 options"
                );
            }

            long correctCount = options
                    .stream()
                    .filter(option ->
                            Boolean.TRUE.equals(option.getCorrect()))
                    .count();

            if (correctCount < 2) {

                throw new IllegalArgumentException(
                        "MSQ must have at least two correct options"
                );
            }
        }


        // -----------------------------------------------------
        // TRUE / FALSE
        // -----------------------------------------------------

        if (type == QuestionType.TRUE_FALSE) {

            if (options == null || options.size() != 2) {

                throw new IllegalArgumentException(
                        "TRUE_FALSE must contain exactly 2 options"
                );
            }
        }


        // -----------------------------------------------------
        // Non-MCQ types
        // -----------------------------------------------------

        if (type != QuestionType.MCQ
                && type != QuestionType.MSQ
                && type != QuestionType.TRUE_FALSE) {

            if (options != null && !options.isEmpty()) {

                throw new IllegalArgumentException(
                        "Options are only allowed for MCQ, MSQ and TRUE_FALSE questions"
                );
            }
        }
    }


    // =========================================================
    // ENTITY → RESPONSE DTO
    // =========================================================

    private QuestionResponse mapToResponse(
            Question question
    ) {

        List<QuestionOptionResponse> options =
                question.getOptions()
                        .stream()
                        .map(option ->
                                QuestionOptionResponse.builder()
                                        .id(option.getId())
                                        .optionLabel(
                                                option.getOptionLabel()
                                        )
                                        .optionText(
                                                option.getOptionText()
                                        )
                                        .correct(
                                                option.getCorrect()
                                        )
                                        .build()
                        )
                        .toList();


        return QuestionResponse.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .answer(question.getAnswer())
                .explanation(question.getExplanation())
                .questionType(question.getQuestionType())
                .difficulty(question.getDifficulty())
                .marks(question.getMarks())
                .bloomLevel(question.getBloomLevel())
                .subjectId(question.getSubject().getId())
                .unitId(question.getUnit().getId())
                .topicId(
                        question.getTopic() != null
                                ? question.getTopic().getId()
                                : null
                )
                .courseOutcomeId(
                        question.getCourseOutcome() != null
                                ? question.getCourseOutcome().getId()
                                : null
                )
                .source(question.getSource())
                .tags(question.getTags())
                .options(options)
                .createdBy(question.getCreatedBy().getId())
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .build();
    }
}