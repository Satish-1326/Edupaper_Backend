package com.edupaper.repository;

import com.edupaper.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findByUnitIdOrderByNameAsc(Long unitId);

    Optional<Topic> findByIdAndUnitSubjectCreatedById(
            Long topicId,
            Long userId
    );
    Optional<Topic> findByIdAndUnitIdAndUnitSubjectCreatedById(
            Long topicId,
            Long unitId,
            Long userId
    );

    boolean existsByUnitIdAndName(
            Long unitId,
            String name
    );

    boolean existsByUnitIdAndNameAndIdNot(
            Long unitId,
            String name,
            Long topicId
    );
}