package com.rgbradford.backend.service.impl;

import com.rgbradford.backend.dto.RegressionResultDto;
import com.rgbradford.backend.dto.StandardCurveDto;
import com.rgbradford.backend.dto.StandardCurvePointDto;
import com.rgbradford.backend.entity.Well;
import com.rgbradford.backend.entity.WellAnalysis;
import com.rgbradford.backend.entity.WellType;
import com.rgbradford.backend.repository.WellRepository;
import com.rgbradford.backend.service.interfaces.StandardCurveService;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StandardCurveServiceImpl implements StandardCurveService {

    private final WellRepository wellRepository;

    public StandardCurveServiceImpl(WellRepository wellRepository) {
        this.wellRepository = wellRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public StandardCurveDto calculateStandardCurve(Long plateLayoutId) {
        // Get all standard wells for this plate layout with their analyses
        List<Well> standardWells = wellRepository.findByPlateLayoutIdAndType(plateLayoutId, WellType.STANDARD);
        
        if (standardWells.isEmpty()) {
            throw new IllegalArgumentException("No standard wells found for the specified plate layout");
        }

        // Group standard wells by concentration
        Map<Double, List<Well>> wellsByConcentration = standardWells.stream()
                .filter(well -> well.getStandardConcentration() != null)
                .collect(Collectors.groupingBy(Well::getStandardConcentration));

        List<StandardCurvePointDto> points = new ArrayList<>();
        
        // Process each concentration group
        for (Map.Entry<Double, List<Well>> entry : wellsByConcentration.entrySet()) {
            List<Well> wells = entry.getValue();
            
            // Filter out wells without analysis
            List<WellAnalysis> analyses = wells.stream()
                    .map(Well::getWellAnalysis)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            if (!analyses.isEmpty()) {
                // Get the standard concentration from the first well (all wells in this group have the same concentration)
                Double standardConcentration = wells.get(0).getStandardConcentration();
                
                if (standardConcentration != null) {
                    // Calculate average blue-to-green ratio for this concentration
                    double avgRatio = analyses.stream()
                            .mapToDouble(WellAnalysis::getBlueToGreenRatio)
                            .average()
                            .orElse(Double.NaN);
                    
                    if (!Double.isNaN(avgRatio)) {
                        points.add(new StandardCurvePointDto(
                                standardConcentration,  // x: BSA concentration (mg/mL)
                                avgRatio               // y: Average blue-to-green ratio
                        ));
                    }
                }
            }
        }
        
        // Sort points by concentration (ascending)
        points.sort(Comparator.comparingDouble(StandardCurvePointDto::getConcentration));
        
        // Calculate linear regression if we have enough points
        RegressionResultDto regression = null;
        
        if (points.size() >= 2) {
            // Prepare data for curve fitting
            WeightedObservedPoints obs = new WeightedObservedPoints();
            points.forEach(point -> obs.add(point.getConcentration(), point.getBlueToGreenRatio()));
            
            // Fit a first degree polynomial (linear regression)
            double[] coefficients = PolynomialCurveFitter.create(1).fit(obs.toList());
            
            // Calculate R²
            double rSquared = calculateRSquared(points, coefficients);
            
            // Create regression result
            regression = new RegressionResultDto(
                coefficients[1], // slope
                coefficients[0], // intercept
                rSquared
            );
        }
        
        return new StandardCurveDto(points, regression);
    }
    
    private Double calculateRSquared(List<StandardCurvePointDto> points, double[] coefficients) {
        if (points.size() < 2) return null;
        
        double a = coefficients[1]; // slope
        double b = coefficients[0]; // intercept
        
        // Calculate mean of y values
        double yMean = points.stream()
                .mapToDouble(StandardCurvePointDto::getBlueToGreenRatio)
                .average()
                .orElse(0);
        
        double ssTot = 0;  // Total sum of squares
        double ssRes = 0;  // Residual sum of squares
        
        for (StandardCurvePointDto point : points) {
            double y = point.getBlueToGreenRatio();
            double yPred = a * point.getConcentration() + b;
            
            ssTot += Math.pow(y - yMean, 2);
            ssRes += Math.pow(y - yPred, 2);
        }
        
        return 1 - (ssRes / ssTot);
    }
}
