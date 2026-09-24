package com.edupaper.repository;

import com.edupaper.entity.PaperQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaperQuestionRepository
        extends JpaRepository<PaperQuestion, Long> {

    List<PaperQuestion> findByPaperIdOrderByQuestionOrderAsc(
            Long paperId
    );

    boolean existsByPaperIdAndQuestionId(
            Long paperId,
            Long questionId
    );

    void deleteByPaperId(Long paperId);
}