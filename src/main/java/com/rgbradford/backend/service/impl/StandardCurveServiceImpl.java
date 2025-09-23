package com.rgbradford.backend.service.impl;

import com.rgbradford.backend.dto.RegressionResultDto;
import com.rgbradford.backend.dto.StandardCurveDto;
import com.rgbradford.backend.dto.StandardCurvePointDto;
import com.rgbradford.backend.entity.*;
import com.rgbradford.backend.repository.CalibrationCurveRepository;
import com.rgbradford.backend.repository.PlateLayoutRepository;
import com.rgbradford.backend.repository.WellRepository;
import com.rgbradford.backend.service.interfaces.StandardCurveService;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class StandardCurveServiceImpl implements StandardCurveService {

    private final WellRepository wellRepository;
    private final CalibrationCurveRepository calibrationCurveRepository;
    private final PlateLayoutRepository plateLayoutRepository;

    public StandardCurveServiceImpl(WellRepository wellRepository,
                                  CalibrationCurveRepository calibrationCurveRepository,
                                  PlateLayoutRepository plateLayoutRepository) {
        this.wellRepository = wellRepository;
        this.calibrationCurveRepository = calibrationCurveRepository;
        this.plateLayoutRepository = plateLayoutRepository;
    }
    
    @Override
    public StandardCurveDto getStandardCurve(Long plateLayoutId) {
        // First try to get from database
        StandardCurveDto storedCurve = getStoredStandardCurve(plateLayoutId);
        if (storedCurve != null) {
            return storedCurve;
        }
        
        // If not found, calculate and store a new one
        return calculateAndStoreStandardCurve(plateLayoutId);
    }
    
    @Override
    public StandardCurveDto getStoredStandardCurve(Long plateLayoutId) {
        return calibrationCurveRepository.findByPlateLayoutId(plateLayoutId)
                .map(this::convertToDto)
                .orElse(null);
    }
    
    private StandardCurveDto convertToDto(CalibrationCurve curve) {
        // Convert the stored calibration curve to DTO format
        // Note: This is a simplified conversion. You might need to adjust based on your actual data structure.
        return new StandardCurveDto(
            Collections.emptyList(), // Points are not stored, only the equation
            new RegressionResultDto(
                curve.getSlope(),
                curve.getIntercept(),
                curve.getRSquared()
            )
        );
    }

    @Override
    @Transactional
    public StandardCurveDto calculateAndStoreStandardCurve(Long plateLayoutId) {
        // Get all standard wells for this plate layout with their analyses
        List<Well> standardWellsList = wellRepository.findByPlateLayoutIdAndType(plateLayoutId, WellType.STANDARD);
        
        if (standardWellsList.isEmpty()) {
            throw new IllegalArgumentException("No standard wells found for the specified plate layout");
        }

        // Group standard wells by concentration
        Map<Double, List<Well>> wellsByConcentration = standardWellsList.stream()
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
        
        StandardCurveDto result = new StandardCurveDto(points, regression);
        
        // Store the calibration curve in the database
        if (regression != null) {
            PlateLayout plateLayout = plateLayoutRepository.findById(plateLayoutId)
                    .orElseThrow(() -> new IllegalArgumentException("Plate layout not found"));
            
            // Create and save the calibration curve
            CalibrationCurve curve = CalibrationCurve.builder()
                    .slope(regression.getSlope())
                    .intercept(regression.getIntercept())
                    .rSquared(regression.getRSquared())
                    .dataPointCount(points.size())
                    .plateLayout(plateLayout)
                    .calibrationWells(standardWellsList) // Use the already loaded standard wells
                    .build();
            
            calibrationCurveRepository.save(curve);
        }
        
        return result;
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
