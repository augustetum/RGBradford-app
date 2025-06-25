package com.rgbradford.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "wells")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Well {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int row;

    @Column(nullable = false)
    private int column;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WellType type;

    // For STANDARD wells
    private Double standardConcentration;

    // For SAMPLE wells
    private String sampleName;
    private Double dilutionFactor;

    // For grouping replicates
    private String replicateGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plate_layout_id", nullable = false)
    private PlateLayout plateLayout;
} 