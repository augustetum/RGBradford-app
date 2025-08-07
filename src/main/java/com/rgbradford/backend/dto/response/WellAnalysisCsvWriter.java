package com.rgbradford.backend.dto.response;

import com.rgbradford.backend.entity.WellAnalysis;
import java.util.List;

public class WellAnalysisCsvWriter {
    public static String toCsvString(List<WellAnalysis> results) {
        StringBuilder csv = new StringBuilder();
        csv.append("Row,Column,Green,Blue,BlueGreenRatio,CalculatedConcentration\n");
        for (WellAnalysis wa : results) {
            csv.append(wa.getWell().getRow()).append(",")
               .append(wa.getWell().getColumn()).append(",")
               .append(wa.getGreenValue() != null ? wa.getGreenValue() : "").append(",")
               .append(wa.getBlueValue() != null ? wa.getBlueValue() : "").append(",")
               .append(wa.getBlueToGreenRatio() != null ? String.format("%.3f", wa.getBlueToGreenRatio()) : "").append(",")
               .append(wa.getCalculatedConcentration() != null ? String.format("%.3f", wa.getCalculatedConcentration()) : "")
               .append("\n");
        }
        return csv.toString();
    }
} 