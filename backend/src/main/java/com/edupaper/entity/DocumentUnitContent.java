package com.edupaper.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "document_unit_contents",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_document_unit",
                        columnNames = {"document_id", "unit_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUnitContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "document_id",
            nullable = false
    )
    private UploadedDocument document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "unit_id",
            nullable = false
    )
    private Unit unit;

    @Column(
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String content;

    @Column(nullable = false)
    private Integer characterCount;

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
}