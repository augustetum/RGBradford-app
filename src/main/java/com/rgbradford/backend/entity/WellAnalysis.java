package com.rgbradford.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

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

    // RGB values (0-255 range)
    private Integer redValue;
    private Integer greenValue;
    private Integer blueValue;

    // Calculated ratios and concentrations
    private Double blueGreenRatio;
    private Double calculatedConcentration;

    // Analysis metadata
    private LocalDateTime analyzedAt;
    private String analysisMethod; // e.g., "Bradford", "BCA", etc.

    @PrePersist
    protected void onCreate() {
        this.analyzedAt = LocalDateTime.now();
    }
} 