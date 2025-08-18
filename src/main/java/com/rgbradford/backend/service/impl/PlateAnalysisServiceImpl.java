package com.rgbradford.backend.service.impl;

import com.rgbradford.backend.dto.request.PlateAnalysisParams;
import com.rgbradford.backend.service.interfaces.PlateAnalysisService;
import com.rgbradford.backend.dto.response.WellAnalysisResult;
import com.rgbradford.backend.entity.PlateLayout;
import com.rgbradford.backend.entity.Well;
import com.rgbradford.backend.entity.WellAnalysis;
import com.rgbradford.backend.repository.PlateLayoutRepository;
import com.rgbradford.backend.repository.WellRepository;
import com.rgbradford.backend.repository.WellAnalysisRepository;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.gui.OvalRoi;
import ij.process.ImageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlateAnalysisServiceImpl implements PlateAnalysisService {

    @Autowired
    private PlateLayoutRepository plateLayoutRepository;
    @Autowired
    private WellRepository wellRepository;
    @Autowired
    private WellAnalysisRepository wellAnalysisRepository;

    @Override
    public List<WellAnalysisResult> analyzePlate(String imagePath, PlateAnalysisParams params) throws Exception {
        try (java.io.InputStream is = new java.io.FileInputStream(imagePath)) {
            return analyzePlate(is, params);
        }
    }

    public List<WellAnalysisResult> analyzePlate(java.io.InputStream imageInputStream, PlateAnalysisParams params) throws Exception {
        validateParameters(params);
        
        int columns = params.getColumns();
        int rows = params.getRows();
        int xOrigin = params.getXOrigin();
        int yOrigin = params.getYOrigin();
        int xEnd = params.getXEnd();
        int yEnd = params.getYEnd();

        // Use 15% reduction for well diameter to avoid edge effects (as per protocol)
        int circleSize = (int) Math.round(params.getWellDiameter() * 0.85);

        ImagePlus imp = openAndPrepareImage(imageInputStream);

        // Calculate grid spacing
        double wellSpacingX = (double)(xEnd - xOrigin) / (columns - 1);
        double wellSpacingY = (double)(yEnd - yOrigin) / (rows - 1);

        ImageProcessor processor = imp.getProcessor();
        List<WellAnalysisResult> results = new ArrayList<>();
        Long counter = 0L;
        
        for (int row = 0; row < rows; row++) {
            String rowLetter = String.valueOf((char)('A' + row));
            for (int col = 0; col < columns; col++) {
                int centerX = (int)(xOrigin + col * wellSpacingX);
                int centerY = (int)(yOrigin + row * wellSpacingY);
                
                OvalRoi roi = createWellROI(centerX, centerY, circleSize);
                processor.setRoi(roi);

                RGBMeasurements measurements = measureRGBChannels(processor, roi);
                BradfordCalculations calculations = calculateBradfordValues(measurements);

                String wellId = rowLetter + (col + 1);

                WellAnalysisResult result = WellAnalysisResult.builder()
                        .wellId(counter)
                        .row(row)
                        .column(col + 1)
                        .greenValue((int)Math.round(measurements.greenMean))
                        .blueValue((int)Math.round(measurements.blueMean))
                        .blueToGreenRatio(calculations.blueToGreenRatio)  // Corrected ratio
                        .greenAbsorbance(calculations.greenAbsorbance)    // Added absorbance values
                        .blueAbsorbance(calculations.blueAbsorbance)
                        .absorbanceRatio(calculations.absorbanceRatio)
                        .pixelCount(measurements.pixelCount)              // Added for quality control
                        .calculatedConcentration(null) // Will be calculated later with standard curve
                        .build();
                results.add(result);
                counter++;
            }
        }
        imp.close();
        return results;
    }

    @Transactional
    public List<WellAnalysis> analyzeAndPersistPlate(Long plateLayoutId, java.io.InputStream imageInputStream, PlateAnalysisParams params) throws Exception {
        validateParameters(params);
        
        PlateLayout plateLayout = findOrCreatePlateLayout(plateLayoutId, params);

        int columns = params.getColumns();
        int rows = params.getRows();
        int xOrigin = params.getXOrigin();
        int yOrigin = params.getYOrigin();
        int xEnd = params.getXEnd();
        int yEnd = params.getYEnd();
        int circleSize = (int) Math.round(params.getWellDiameter() * 0.85);

        ImagePlus imp = openAndPrepareImage(imageInputStream);
        
        double wellSpacingX = (double)(xEnd - xOrigin) / (columns - 1);
        double wellSpacingY = (double)(yEnd - yOrigin) / (rows - 1);

        ImageProcessor processor = imp.getProcessor();
        List<WellAnalysis> results = new ArrayList<>();
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                final int finalRow = row;
                final int finalCol = col;
                
                int centerX = (int)(xOrigin + col * wellSpacingX);
                int centerY = (int)(yOrigin + row * wellSpacingY);
                
                OvalRoi roi = createWellROI(centerX, centerY, circleSize);
                processor.setRoi(roi);
                RGBMeasurements measurements = measureRGBChannels(processor, roi);
                BradfordCalculations calculations = calculateBradfordValues(measurements);

                Well well = findOrCreateWell(plateLayout, finalRow, finalCol);
                WellAnalysis wellAnalysis = findOrCreateWellAnalysis(well);
                
                // Set analysis values with correct calculations
                wellAnalysis.setGreenValue((int)Math.round(measurements.greenMean));
                wellAnalysis.setBlueValue((int)Math.round(measurements.blueMean));
                wellAnalysis.setBlueToGreenRatio(calculations.blueToGreenRatio);  // Corrected
                wellAnalysis.setGreenAbsorbance(calculations.greenAbsorbance);
                wellAnalysis.setBlueAbsorbance(calculations.blueAbsorbance);
                wellAnalysis.setAbsorbanceRatio(calculations.absorbanceRatio);
                wellAnalysis.setPixelCount(measurements.pixelCount);

                if (wellAnalysis.getId() == null) {
                    wellAnalysisRepository.save(wellAnalysis);
                }
                results.add(wellAnalysis);
            }
        }
        imp.close();
        return results;
    }
    
    // New method to calculate Bradford-specific values according to protocol
    private BradfordCalculations calculateBradfordValues(RGBMeasurements measurements) {
        BradfordCalculations calc = new BradfordCalculations();
        
        // Calculate blue-to-green ratio as per RGBradford protocol
        calc.blueToGreenRatio = (measurements.greenMean > 0) ? 
            measurements.blueMean / measurements.greenMean : 0.0;
        
        // Calculate absorbances (negative log base 10 of transmittance)
        // Using 255 as reference (100% transmittance for 8-bit RGB)
        calc.greenAbsorbance = (measurements.greenMean > 0) ? 
            -Math.log10(measurements.greenMean / 255.0) : Double.MAX_VALUE;
        calc.blueAbsorbance = (measurements.blueMean > 0) ? 
            -Math.log10(measurements.blueMean / 255.0) : Double.MAX_VALUE;
        
        // Calculate absorbance ratio (green/blue absorbance)
        calc.absorbanceRatio = (calc.blueAbsorbance > 0 && calc.blueAbsorbance != Double.MAX_VALUE) ? 
            calc.greenAbsorbance / calc.blueAbsorbance : 0.0;
            
        return calc;
    }
    
    // Helper method to validate input parameters
    private void validateParameters(PlateAnalysisParams params) {
        if (params.getColumns() <= 0 || params.getRows() <= 0) {
            throw new IllegalArgumentException("Plate dimensions must be positive");
        }
        if (params.getWellDiameter() <= 0) {
            throw new IllegalArgumentException("Well diameter must be positive");
        }
        if (params.getXOrigin() >= params.getXEnd() || params.getYOrigin() >= params.getYEnd()) {
            throw new IllegalArgumentException("Invalid coordinate range");
        }
    }
    
    // Helper method to open and prepare image
    private ImagePlus openAndPrepareImage(java.io.InputStream imageInputStream) throws IOException {
        // Try to read as BufferedImage and wrap in ImagePlus
        javax.imageio.ImageIO.setUseCache(false);
        java.awt.image.BufferedImage bufferedImage = javax.imageio.ImageIO.read(imageInputStream);
        if (bufferedImage == null) {
            throw new IOException("Could not decode image from input stream");
        }
        ImagePlus imp = new ImagePlus("uploaded", bufferedImage);
        // Ensure RGB as per protocol
        if (imp.getType() != ImagePlus.COLOR_RGB) {
            ImageConverter ic = new ImageConverter(imp);
            ic.convertToRGB();
        }
        return imp;
    }
    
    // Helper method to create well ROI
    private OvalRoi createWellROI(int centerX, int centerY, int circleSize) {
        int roiX = centerX - circleSize/2;
        int roiY = centerY - circleSize/2;
        return new OvalRoi(roiX, roiY, circleSize, circleSize);
    }
    
    // Helper methods for database operations
    private PlateLayout findOrCreatePlateLayout(Long plateLayoutId, PlateAnalysisParams params) {
        return plateLayoutRepository.findById(plateLayoutId)
                .orElseGet(() -> {
                    PlateLayout pl = PlateLayout.builder()
                        .id(plateLayoutId)
                        .rows(params.getRows())
                        .columns(params.getColumns())
                        .build();
                    return plateLayoutRepository.save(pl);
                });
    }
    
    private Well findOrCreateWell(PlateLayout plateLayout, int row, int col) {
        return wellRepository.findByPlateLayoutIdAndRowAndColumn(plateLayout.getId(), row, col)
                .orElseGet(() -> {
                    Well w = Well.builder()
                        .row(row)
                        .column(col)
                        .plateLayout(plateLayout)
                        .build();
                    return wellRepository.save(w);
                });
    }
    
    private WellAnalysis findOrCreateWellAnalysis(Well well) {
        return wellAnalysisRepository.findByWellId(well.getId())
                .orElseGet(() -> WellAnalysis.builder()
                    .well(well)
                    .build());
    }
    
    // Enhanced RGB measurement method with better error handling
    private static RGBMeasurements measureRGBChannels(ImageProcessor processor, OvalRoi roi) {
        Rectangle bounds = roi.getBounds();

        int greenSum = 0, blueSum = 0;
        int pixelCount = 0;
        int validPixelCount = 0;

        for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
            for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
                if (roi.contains(x, y)) {
                    pixelCount++;
                    // Check bounds to avoid errors
                    if (x >= 0 && x < processor.getWidth() && y >= 0 && y < processor.getHeight()) {
                        int rgb = processor.getPixel(x, y);
                        int green = (rgb >> 8) & 0xFF;
                        int blue = rgb & 0xFF;
                        
                        greenSum += green;
                        blueSum += blue;
                        validPixelCount++;
                    }
                }
            }
        }

        RGBMeasurements result = new RGBMeasurements();
        if (validPixelCount > 0) {
            result.greenMean = (double)greenSum / validPixelCount;
            result.blueMean = (double)blueSum / validPixelCount;
            result.pixelCount = validPixelCount;
            result.totalPixelsInROI = pixelCount;
        }
        
        return result;
    }

    // Enhanced measurement class with additional fields
    private static class RGBMeasurements {
        double greenMean = 0;
        double blueMean = 0;
        int pixelCount = 0;
        int totalPixelsInROI = 0;  // For quality control
    }
    
    // New class to hold Bradford-specific calculations
    private static class BradfordCalculations {
        double blueToGreenRatio = 0.0;
        double greenAbsorbance = 0.0;
        double blueAbsorbance = 0.0;
        double absorbanceRatio = 0.0;
    }
}