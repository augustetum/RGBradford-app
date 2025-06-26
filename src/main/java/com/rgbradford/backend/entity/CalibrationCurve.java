package com.rgbradford.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Entity
@Table(name = "calibration_curves")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalibrationCurve {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    //Mathematical parameters of the calibration curve (y = mx + b)
    @Column(nullable = false)
    private Double slope; //m

    @Column(nullable = false)
    private Double intercept; //b

    //Goodness of fit (R^2)
    @Column(name = "r_squared")
    private Double rSquared;

    //Standard error of the estimate
    @Column(name = "standard_error")
    private Double standardError;

    //Number of data points used to create the curve
    @Column(name = "data_point_count")
    private Integer dataPointCount;

    //The plate layout this calibration curve belongs to (one curve per plate)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plate_layout_id", nullable = false)
    private PlateLayout plateLayout;

    //The specific wells used to create this calibration curve 
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "calibration_curve_wells",
        joinColumns = @JoinColumn(name = "calibration_curve_id"),
        inverseJoinColumns = @JoinColumn(name = "well_id")
    )
    private List<Well> calibrationWells;
} 