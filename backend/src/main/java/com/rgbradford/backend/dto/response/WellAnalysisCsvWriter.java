package com.rgbradford.backend.dto.response;

import com.rgbradford.backend.dto.StandardCurveDto;
import com.rgbradford.backend.dto.StandardCurvePointDto;
import com.rgbradford.backend.dto.RegressionResultDto;
import com.rgbradford.backend.entity.WellAnalysis;
import com.rgbradford.backend.entity.WellType;
import java.util.List;

public class WellAnalysisCsvWriter {
    // Backward compatibility overload
    public static String toCsvString(List<WellAnalysis> results) {
        return toCsvString(results, null);
    }

    public static String toCsvString(List<WellAnalysis> results, StandardCurveDto curve) {
        StringBuilder csv = new StringBuilder();

        // Section 1: Calibration Points (if provided)
        if (curve != null && curve.getPoints() != null) {
            List<StandardCurvePointDto> points = curve.getPoints();
            csv.append("Calibration Points\n");
            csv.append("BlueGreenRatio (x),Concentration (y)\n");
            for (StandardCurvePointDto p : points) {
                csv.append(formatDouble(p.getBlueToGreenRatio())).append(",")
                   .append(formatDouble(p.getConcentration()))
                   .append("\n");
            }

            // Slope/Intercept/R^2 if available
            RegressionResultDto reg = curve.getRegression();
            if (reg != null) {
                csv.append("\n");
                csv.append("Slope (m),").append(formatDouble(reg.getSlope())).append(",")
                   .append("Intercept (b),").append(formatDouble(reg.getIntercept())).append(",")
                   .append("R^2,").append(formatDouble(reg.getRSquared()))
                   .append("\n");
            }

            csv.append("\n");
        }

        // Section 2: Well Analysis
        csv.append("Well Analysis\n");
        csv.append("Row,Column,BlueGreenRatio,CalculatedConcentration_mxb\n");

        Double m = null, b = null;
        if (curve != null && curve.getRegression() != null) {
            m = curve.getRegression().getSlope();
            b = curve.getRegression().getIntercept();
        }

        for (WellAnalysis wa : results) {
            // Include only SAMPLE wells; skip EMPTY and all other types
            if (wa.getWell() == null || wa.getWell().getType() != WellType.SAMPLE) {
                continue;
            }
            double ratio = wa.getBlueToGreenRatio() != null ? wa.getBlueToGreenRatio() : Double.NaN;
            String calcConc;
            if (m != null && b != null && !Double.isNaN(ratio)) {
                calcConc = formatDouble(m * ratio + b);
            } else {
                // Fallback to stored value
                calcConc = wa.getCalculatedConcentration() != null ? formatDouble(wa.getCalculatedConcentration()) : "";
            }

            // Skip empty wells: neither ratio nor calculated concentration is available
            boolean hasRatio = !Double.isNaN(ratio);
            boolean hasCalc = calcConc != null && !calcConc.isEmpty();
            if (!hasRatio && !hasCalc) {
                continue;
            }

            csv.append(wa.getWell().getRow()).append(",")
               .append(wa.getWell().getColumn()).append(",")
               .append(wa.getBlueToGreenRatio() != null ? formatDouble(wa.getBlueToGreenRatio()) : "").append(",")
               .append(calcConc)
               .append("\n");
        }
        return csv.toString();
    }

    private static String formatDouble(Double d) {
        if (d == null) return "";
        return String.format("%.4f", d);
    }
} 