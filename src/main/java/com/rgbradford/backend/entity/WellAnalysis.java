package com.rgbradford.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "well_analyses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WellAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "well_id", nullable = false, unique = true)
    private Well well;

    //RGB values (0-255 range)
    private Integer greenValue;
    private Integer blueValue;

    //Calculated ratios and concentrations
    private Double blueToGreenRatio;
    private Double calculatedConcentration;
    // Extended Bradford analysis fields
    private Double greenAbsorbance;
    private Double blueAbsorbance;
    private Double absorbanceRatio;
    private Integer pixelCount;

} 