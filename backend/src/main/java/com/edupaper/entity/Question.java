package com.edupaper.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // Main question
    @Column(nullable = false, length = 5000)
    private String questionText;


    // Correct answer / expected answer
    @Column(length = 5000)
    private String answer;


    // Explanation for the answer
    @Column(length = 5000)
    private String explanation;


    // Type of question
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType questionType;


    // Difficulty
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;


    // Marks
    @Column(nullable = false)
    private Integer marks;


    // Bloom's Taxonomy level
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloomLevel bloomLevel;


    // Subject
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;


    // Unit
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;


    // Topic
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;


    // Course Outcome
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_outcome_id")
    private CourseOutcome courseOutcome;


    // Source of question
    private String source;


    // Tags
    @Column(length = 1000)
    private String tags;


    // User who created the question
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;


    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }


    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @OneToMany(
            mappedBy = "question",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<QuestionOption> options = new ArrayList<>();

    @OneToMany(
            mappedBy = "question",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<PaperQuestion> paperQuestions = new ArrayList<>();

}