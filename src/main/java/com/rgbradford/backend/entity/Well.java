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

    //Standard wells additional field
    private Double standardConcentration;

    //Sample wells additional fields
    private String sampleName;
    private Double dilutionFactor;

    //Groups of replicates (optional)
    private String replicateGroup;

    //Image analysis results
    private String rgbValue;           //Raw RGB values from image
    private Double blueGreenRatio;     //Calculated B/G ratio
    private Double calculatedConcentration; //Final calculated concentration (for samples)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plate_layout_id", nullable = false)
    private PlateLayout plateLayout;
} 