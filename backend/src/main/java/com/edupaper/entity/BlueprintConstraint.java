package com.edupaper.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "blueprint_constraints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlueprintConstraint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlueprintConstraintType constraintType;


    @Column(nullable = false)
    private String value;


    @Column(nullable = false)
    private Integer requiredCount;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blueprint_id", nullable = false)
    private Blueprint blueprint;
}