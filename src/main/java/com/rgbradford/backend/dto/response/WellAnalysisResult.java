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
    private String wellId;
    private String row;
    private int column;
    private int xCenter;
    private int yCenter;
    private double greenIntensity;
    private double greenAbsorbance;
    private double blueIntensity;
    private double blueAbsorbance;
    private double gbRatio;
    private double abRatio;
} 