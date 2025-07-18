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
import ij.io.FileSaver;
import ij.process.ImageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    // Refactored: returns list of WellAnalysisResult instead of writing CSV
    public List<WellAnalysisResult> analyzePlate(String imagePath, PlateAnalysisParams params) throws Exception {
        int columns = params.getColumns();
        int rows = params.getRows();
        int xOrigin = params.getXOrigin();
        int yOrigin = params.getYOrigin();
        int xEnd = params.getXEnd();
        int yEnd = params.getYEnd();

        // Use 15% reduction for well diameter to avoid edge effects
        int circleSize = (int) Math.round(params.getWellDiameter() * 0.85);

        // Open the image
        ImagePlus imp = ij.IJ.openImage(imagePath);
        if (imp == null) {
            throw new IOException("Could not open image at: " + imagePath);
        }

        // Ensure RGB
        if (imp.getType() != ImagePlus.COLOR_RGB) {
            ImageConverter ic = new ImageConverter(imp);
            ic.convertToRGB();
        }

        // Calculate grid spacing
        double wellSpacingX = (double)(xEnd - xOrigin) / (columns - 1);
        double wellSpacingY = (double)(yEnd - yOrigin) / (rows - 1);

        ImageProcessor processor = imp.getProcessor();
        List<WellAnalysisResult> results = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            String rowLetter = String.valueOf((char)('A' + row));
            for (int col = 0; col < columns; col++) {
                int centerX = (int)(xOrigin + col * wellSpacingX);
                int centerY = (int)(yOrigin + row * wellSpacingY);
                int roiX = centerX - circleSize/2;
                int roiY = centerY - circleSize/2;
                OvalRoi roi = new OvalRoi(roiX, roiY, circleSize, circleSize);
                processor.setRoi(roi);

                RGBMeasurements measurements = measureRGBChannels(processor, roi);

                String wellId = rowLetter + (col + 1);
                double greenAbsorbance = (measurements.greenMean > 0) ?
                        -(Math.log(measurements.greenMean / 255.0) / Math.log(10)) : 999;
                double blueAbsorbance = (measurements.blueMean > 0) ?
                        -(Math.log(measurements.blueMean / 255.0) / Math.log(10)) : 999;
                double gbRatio = (measurements.blueMean > 0) ?
                        measurements.greenMean / measurements.blueMean : 0;
                double abRatio = (blueAbsorbance > 0 && blueAbsorbance != 999) ?
                        greenAbsorbance / blueAbsorbance : 0;

                WellAnalysisResult result = WellAnalysisResult.builder()
                        .wellId(wellId)
                        .row(rowLetter)
                        .column(col + 1)
                        .xCenter(centerX)
                        .yCenter(centerY)
                        .greenIntensity(measurements.greenMean)
                        .greenAbsorbance(greenAbsorbance)
                        .blueIntensity(measurements.blueMean)
                        .blueAbsorbance(blueAbsorbance)
                        .gbRatio(gbRatio)
                        .abRatio(abRatio)
                        .build();
                results.add(result);
            }
        }
        imp.close();
        return results;
    }

    @Transactional
    public List<WellAnalysis> analyzeAndPersistPlate(Long plateLayoutId, String imagePath, PlateAnalysisParams params) throws Exception {
        PlateLayout plateLayout = plateLayoutRepository.findById(plateLayoutId)
                .orElseGet(() -> {
                    PlateLayout pl = PlateLayout.builder()
                        .id(plateLayoutId)
                        .rows(params.getRows())
                        .columns(params.getColumns())
                        .build();
                    return plateLayoutRepository.save(pl);
                });

        int columns = params.getColumns();
        int rows = params.getRows();
        int xOrigin = params.getXOrigin();
        int yOrigin = params.getYOrigin();
        int xEnd = params.getXEnd();
        int yEnd = params.getYEnd();
        int circleSize = (int) Math.round(params.getWellDiameter() * 0.85);

        ImagePlus imp = ij.IJ.openImage(imagePath);
        if (imp == null) {
            throw new IOException("Could not open image at: " + imagePath);
        }
        if (imp.getType() != ImagePlus.COLOR_RGB) {
            ImageConverter ic = new ImageConverter(imp);
            ic.convertToRGB();
        }
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
                int roiX = centerX - circleSize/2;
                int roiY = centerY - circleSize/2;
                OvalRoi roi = new OvalRoi(roiX, roiY, circleSize, circleSize);
                processor.setRoi(roi);
                RGBMeasurements measurements = measureRGBChannels(processor, roi);

                // Find or create Well
                Well well = wellRepository.findByPlateLayoutIdAndRowAndColumn(plateLayout.getId(), finalRow, finalCol)
                        .orElseGet(() -> {
                            Well w = Well.builder()
                                .row(finalRow)
                                .column(finalCol)
                                .plateLayout(plateLayout)
                                .build();
                            return wellRepository.save(w);
                        });

                // Find or create WellAnalysis
                WellAnalysis wellAnalysis = wellAnalysisRepository.findByWellId(well.getId())
                        .orElseGet(() -> {
                            WellAnalysis wa = WellAnalysis.builder()
                                .well(well)
                                .build();
                            return wa;
                        });
                // Set analysis values
                wellAnalysis.setRedValue((int)Math.round(measurements.redMean));
                wellAnalysis.setGreenValue((int)Math.round(measurements.greenMean));
                wellAnalysis.setBlueValue((int)Math.round(measurements.blueMean));
                double blueGreenRatio = (measurements.blueMean > 0) ? measurements.greenMean / measurements.blueMean : 0;
                wellAnalysis.setBlueGreenRatio(blueGreenRatio);
                // You can add more calculations here as needed

                // Save WellAnalysis if new
                if (wellAnalysis.getId() == null) {
                    wellAnalysisRepository.save(wellAnalysis);
                }
                results.add(wellAnalysis);
            }
        }
        imp.close();
        return results;
    }
    
    private static RGBMeasurements measureRGBChannels(ImageProcessor processor, OvalRoi roi) {
        Rectangle bounds = roi.getBounds();
        double centerX = bounds.x + bounds.width / 2.0;
        double centerY = bounds.y + bounds.height / 2.0;
        double radius = bounds.width / 2.0;

        int greenSum = 0, blueSum = 0, redSum = 0;
        int pixelCount = 0;

        for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
            for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
                if (roi.contains(x, y)) {
                    if (x >= 0 && x < processor.getWidth() && y >= 0 && y < processor.getHeight()) {
                        int rgb = processor.getPixel(x, y);
                        int red = (rgb >> 16) & 0xFF;
                        int green = (rgb >> 8) & 0xFF;
                        int blue = rgb & 0xFF;
                        redSum += red;
                        greenSum += green;
                        blueSum += blue;
                        pixelCount++;
                    }
                }
            }
        }

        RGBMeasurements result = new RGBMeasurements();
        if (pixelCount > 0) {
            result.redMean = (double)redSum / pixelCount;
            result.greenMean = (double)greenSum / pixelCount;
            result.blueMean = (double)blueSum / pixelCount;
            result.pixelCount = pixelCount;
        }
        return result;
    }

    private static class RGBMeasurements {
        double redMean = 0;
        double greenMean = 0;
        double blueMean = 0;
        int pixelCount = 0;
    }
}