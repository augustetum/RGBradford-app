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

    //Position of the well on the plate
    @Column(nullable = false)
    private int row;

    @Column(nullable = false)
    private int column;
    
    // Human-readable position (e.g., "A1", "B2")
    @Column(name = "position", length = 5, nullable = false)
    private String position;

    //Type of well (standard, sample, blank, empty)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WellType type;

    //Standard wells additional field
    private Double standardConcentration;

    //Sample wells additional fields
    private String sampleName;
    private Double dilutionFactor;

    //Groups of replicates (optional)
    private String replicateGroup;

    //Reference to the plate layout that this well belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plate_layout_id", nullable = false)
    private PlateLayout plateLayout;

  // Remove the @JoinColumn - use mappedBy instead
@OneToOne(mappedBy = "well", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private WellAnalysis wellAnalysis;
} 