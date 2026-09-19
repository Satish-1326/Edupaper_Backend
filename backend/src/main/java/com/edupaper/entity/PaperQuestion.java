package com.edupaper.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "paper_questions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_paper_question",
                        columnNames = {"paper_id", "question_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaperQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer questionOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false)
    private Paper paper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
}