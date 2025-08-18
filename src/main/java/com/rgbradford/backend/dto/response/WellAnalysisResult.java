package com.rgbradford.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WellAnalysisResult {
    private Long id;
    private Long wellId;
    private int row;
    private int column;
    private String wellType;
    private String sampleName;
    private Integer greenValue;
    private Integer blueValue;
    private Double blueToGreenRatio;
    private Double calculatedConcentration;
    // Added for extended Bradford analysis
    private Double greenAbsorbance;
    private Double blueAbsorbance;
    private Double absorbanceRatio;
    private Integer pixelCount;
} 