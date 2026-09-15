package com.edupaper.repository;

import com.edupaper.entity.Question;
import com.edupaper.entity.BloomLevel;
import com.edupaper.entity.Difficulty;
import com.edupaper.entity.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository
        extends JpaRepository<Question, Long> {

    // Get all questions belonging to a teacher
    List<Question> findByCreatedByIdOrderByCreatedAtDesc(
            Long userId
    );

    // Get one question only if it belongs to the teacher
    Optional<Question> findByIdAndCreatedById(
            Long questionId,
            Long userId
    );

    // Get questions for a subject
    List<Question> findBySubjectIdAndCreatedById(
            Long subjectId,
            Long userId
    );

    // Get questions for a unit
    List<Question> findByUnitIdAndCreatedById(
            Long unitId,
            Long userId
    );

    // Get questions for a topic
    List<Question> findByTopicIdAndCreatedById(
            Long topicId,
            Long userId
    );

    // Get questions for a Course Outcome
    List<Question> findByCourseOutcomeIdAndCreatedById(
            Long courseOutcomeId,
            Long userId
    );

    // Filter by Bloom Level
    List<Question> findByBloomLevelAndCreatedById(
            BloomLevel bloomLevel,
            Long userId
    );

    // Filter by difficulty
    List<Question> findByDifficultyAndCreatedById(
            Difficulty difficulty,
            Long userId
    );

    // Filter by question type
    List<Question> findByQuestionTypeAndCreatedById(
            QuestionType questionType,
            Long userId
    );

    // Count questions by Bloom level
    long countByBloomLevelAndCreatedById(
            BloomLevel bloomLevel,
            Long userId
    );

    // Count questions by difficulty
    long countByDifficultyAndCreatedById(
            Difficulty difficulty,
            Long userId
    );

    // Count questions by question type
    long countByQuestionTypeAndCreatedById(
            QuestionType questionType,
            Long userId
    );
}