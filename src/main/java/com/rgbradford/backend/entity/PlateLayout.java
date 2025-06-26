package com.rgbradford.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Entity
@Table(name = "plate_layouts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlateLayout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int rows;

    @Column(nullable = false)
    private int columns;

    @OneToMany(mappedBy = "plateLayout", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Well> wells;

    //One calibration curve per plate (derived from standards on this plate)
    @OneToOne(mappedBy = "plateLayout", cascade = CascadeType.ALL, orphanRemoval = true)
    private CalibrationCurve calibrationCurve;

    // One project per plate layout
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
} 