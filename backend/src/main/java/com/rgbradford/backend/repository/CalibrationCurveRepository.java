package com.rgbradford.backend.repository;

import com.rgbradford.backend.entity.CalibrationCurve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CalibrationCurveRepository extends JpaRepository<CalibrationCurve, Long> {
    
    
    //Find calibration curve for a specific plate layout
    Optional<CalibrationCurve> findByPlateLayoutId(Long plateLayoutId);

    //Find calibration curves by project ID (through plate layouts)
    List<CalibrationCurve> findByPlateLayoutProjectId(Long projectId);
} 