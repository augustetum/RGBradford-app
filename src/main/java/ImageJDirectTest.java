import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.*;
import java.io.*;
import java.nio.file.*;
import java.awt.*;

public class ImageJDirectTest {
    
    public static void main(String[] args) {
        // Configuration
        String imagePath = "/Users/augustetumaite/repos/RGBradford-app/fiji/plate.jpg";
        String outputPath = "/Users/augustetumaite/repos/RGBradford-app/fiji/plate_results.csv";
        
        // Plate parameters
        int columns = 12;
        int rows = 8;
        int xOrigin = 294;
        int yOrigin = 220;
        int xEnd = 1434;
        int yEnd = 926;
        int circleSize = 80;
        
        try {
            // Check if image exists
            if (!Files.exists(Paths.get(imagePath))) {
                System.err.println("ERROR: Image file not found at: " + imagePath);
                return;
            }
            
            System.out.println("Starting ImageJ direct analysis...");
            System.out.println("Image: " + imagePath);
            System.out.println("Grid: " + columns + "x" + rows + " wells");
            System.out.println("Origin: (" + xOrigin + ", " + yOrigin + ")");
            System.out.println("End: (" + xEnd + ", " + yEnd + ")");
            System.out.println("Circle size: " + circleSize);
            System.out.println();
            
            // Set headless mode before any ImageJ operations
            System.setProperty("java.awt.headless", "true");
            
            // Open the image directly without initializing ImageJ GUI
            ImagePlus imp = IJ.openImage(imagePath);
            if (imp == null) {
                System.err.println("ERROR: Could not open image at: " + imagePath);
                return;
            }
            
            System.out.println("Image loaded: " + imp.getTitle());
            System.out.println("Dimensions: " + imp.getWidth() + "x" + imp.getHeight());
            System.out.println("Type: " + imp.getType());
            System.out.println();
            
            // Ensure we have an RGB image
            if (imp.getType() != ImagePlus.COLOR_RGB) {
                System.out.println("Converting to RGB...");
                ImageConverter ic = new ImageConverter(imp);
                ic.convertToRGB();
            }
            
            // Calculate grid spacing
            double wellSpacingX = (double)(xEnd - xOrigin) / (columns - 1);
            double wellSpacingY = (double)(yEnd - yOrigin) / (rows - 1);
            
            System.out.println("Well spacing: X=" + String.format("%.2f", wellSpacingX) + 
                             ", Y=" + String.format("%.2f", wellSpacingY));
            System.out.println();
            
            // Process each well and collect results
            StringBuilder csvContent = new StringBuilder();
            
            // Add CSV header
            csvContent.append("Well,Row,Column,X_Center,Y_Center,")
                     .append("Green_Intensity,Green_Absorbance,")
                     .append("Blue_Intensity,Blue_Absorbance,")
                     .append("GB_Ratio,AB_Ratio\n");
            
            // Get the image processor
            ImageProcessor processor = imp.getProcessor();
            
            // Process each well
            int wellNumber = 1;
            for (int row = 0; row < rows; row++) {
                String rowLetter = String.valueOf((char)('A' + row));
                
                for (int col = 0; col < columns; col++) {
                    // Calculate well center
                    int centerX = (int)(xOrigin + col * wellSpacingX);
                    int centerY = (int)(yOrigin + row * wellSpacingY);
                    
                    // Create circular ROI
                    int roiX = centerX - circleSize/2;
                    int roiY = centerY - circleSize/2;
                    
                    OvalRoi roi = new OvalRoi(roiX, roiY, circleSize, circleSize);
                    processor.setRoi(roi);
                    
                    // Calculate measurements manually for RGB channels
                    RGBMeasurements measurements = measureRGBChannels(processor, roi);
                    
                    // Well identification
                    String wellId = rowLetter + (col + 1);
                    
                    // Calculate absorbance values
                    double greenAbsorbance = (measurements.greenMean > 0) ? 
                        -(Math.log(measurements.greenMean / 255.0) / Math.log(10)) : 999;
                    double blueAbsorbance = (measurements.blueMean > 0) ? 
                        -(Math.log(measurements.blueMean / 255.0) / Math.log(10)) : 999;
                    
                    // Calculate ratios
                    double gbRatio = (measurements.blueMean > 0) ? 
                        measurements.greenMean / measurements.blueMean : 0;
                    double abRatio = (blueAbsorbance > 0 && blueAbsorbance != 999) ? 
                        greenAbsorbance / blueAbsorbance : 0;
                    
                    // Add to CSV
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
                    
                    // Progress indicator
                    if (wellNumber % 24 == 0 || wellNumber == rows * columns) {
                        System.out.println("Processed " + wellNumber + "/" + (rows * columns) + " wells...");
                    }
                    
                    wellNumber++;
                }
            }
            
            System.out.println();
            System.out.println("Analysis complete! Processing " + (rows * columns) + " wells.");
            
            // Save results to CSV
            try {
                Files.write(Paths.get(outputPath), csvContent.toString().getBytes());
                System.out.println("Results saved to: " + outputPath);
                
                // Display summary statistics
                System.out.println();
                System.out.println("=== SUMMARY STATISTICS ===");
                
                String[] lines = csvContent.toString().split("\n");
                double totalGreen = 0, totalBlue = 0;
                int count = 0;
                
                // Calculate averages (skip header)
                for (int i = 1; i < lines.length; i++) {
                    if (lines[i].trim().isEmpty()) continue;
                    String[] parts = lines[i].split(",");
                    if (parts.length >= 8) {
                        try {
                            totalGreen += Double.parseDouble(parts[5]);
                            totalBlue += Double.parseDouble(parts[7]);
                            count++;
                        } catch (NumberFormatException e) {
                            // Skip invalid lines
                        }
                    }
                }
                
                if (count > 0) {
                    double avgGreen = totalGreen / count;
                    double avgBlue = totalBlue / count;
                    System.out.println("Average Green intensity: " + String.format("%.3f", avgGreen));
                    System.out.println("Average Blue intensity: " + String.format("%.3f", avgBlue));
                    System.out.println("Green/Blue ratio: " + String.format("%.3f", avgGreen/avgBlue));
                }
                
                // Show first few results
                System.out.println();
                System.out.println("=== FIRST 5 WELLS ===");
                System.out.println("Well\tGreen\tBlue\tG/B Ratio");
                for (int i = 1; i <= Math.min(5, lines.length - 1); i++) {
                    if (lines[i].trim().isEmpty()) continue;
                    String[] parts = lines[i].split(",");
                    if (parts.length >= 10) {
                        System.out.printf("%s\t%.3f\t%.3f\t%.3f%n",
                            parts[0], 
                            Double.parseDouble(parts[5]),
                            Double.parseDouble(parts[7]),
                            Double.parseDouble(parts[9]));
                    }
                }
                
            } catch (IOException e) {
                System.err.println("ERROR: Could not save results: " + e.getMessage());
            }
            
            // Clean up
            imp.close();
            System.out.println();
            System.out.println("Analysis completed successfully!");
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Manually measure RGB channels within a circular ROI
     */
    private static RGBMeasurements measureRGBChannels(ImageProcessor processor, OvalRoi roi) {
        Rectangle bounds = roi.getBounds();
        double centerX = bounds.x + bounds.width / 2.0;
        double centerY = bounds.y + bounds.height / 2.0;
        double radius = bounds.width / 2.0;
        
        int greenSum = 0, blueSum = 0, redSum = 0;
        int pixelCount = 0;
        
        // Iterate through the bounding rectangle
        for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
            for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
                // Check if pixel is within the circle
                double dx = x - centerX;
                double dy = y - centerY;
                if (dx*dx + dy*dy <= radius*radius) {
                    // Check bounds
                    if (x >= 0 && x < processor.getWidth() && y >= 0 && y < processor.getHeight()) {
                        int rgb = processor.getPixel(x, y);
                        
                        // Extract RGB components
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
    
    /**
     * Helper class to store RGB measurement results
     */
    private static class RGBMeasurements {
        double redMean = 0;
        double greenMean = 0;
        double blueMean = 0;
        int pixelCount = 0;
    }
}