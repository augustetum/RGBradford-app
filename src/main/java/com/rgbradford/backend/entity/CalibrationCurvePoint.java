package com.rgbradford.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "calibration_curve_points")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalibrationCurvePoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calibration_curve_id", nullable = false)
    private CalibrationCurve calibrationCurve;

    @Column(nullable = false)
    private Double concentration; // x

    @Column(name = "blue_to_green_ratio", nullable = false)
    private Double blueToGreenRatio; // y
}
