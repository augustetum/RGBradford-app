package com.rgbradford.backend.service.impl;

import com.rgbradford.backend.dto.request.PlateAnalysisParams;
import com.rgbradford.backend.service.interfaces.PlateAnalysisService;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.gui.OvalRoi;
import ij.io.FileSaver;
import ij.process.ImageConverter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class PlateAnalysisServiceImpl implements PlateAnalysisService {

    @Override
    public String analyzePlate(String imagePath, PlateAnalysisParams params) throws Exception {
        String outputPath = imagePath.replace(".jpg", "_results.csv");

        int columns = params.getColumns();
        int rows = params.getRows();
        int xOrigin = params.getXOrigin();
        int yOrigin = params.getYOrigin();
        int xEnd = params.getXEnd();
        int yEnd = params.getYEnd();

        // Use 15% reduction for well diameter to avoid edge effects
        int circleSize = (int) Math.round(params.getWellDiameter() * 0.95);

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

        // Prepare CSV
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("Well,Row,Column,X_Center,Y_Center,")
                  .append("Green_Intensity,Green_Absorbance,")
                  .append("Blue_Intensity,Blue_Absorbance,")
                  .append("GB_Ratio,AB_Ratio\n");

        ImageProcessor processor = imp.getProcessor();
        int wellNumber = 1;
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

                csvContent.append(wellId).append(",")
                        .append(rowLetter).append(",")
                        .append(col + 1).append(",")
                        .append(centerX).append(",")
                        .append(centerY).append(",")
                        .append(String.format("%.3f", measurements.greenMean)).append(",")
                        .append(String.format("%.3f", greenAbsorbance)).append(",")
                        .append(String.format("%.3f", measurements.blueMean)).append(",")
                        .append(String.format("%.3f", blueAbsorbance)).append(",")
                        .append(String.format("%.3f", gbRatio)).append(",")
                        .append(String.format("%.3f", abRatio)).append("\n");

                wellNumber++;
            }
        }

        // Save results to CSV
        Files.write(Paths.get(outputPath), csvContent.toString().getBytes());
        imp.close();

        return outputPath;
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