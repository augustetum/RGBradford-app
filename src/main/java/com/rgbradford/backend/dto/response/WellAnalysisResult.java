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
    private Integer redValue;
    private Integer greenValue;
    private Integer blueValue;
    private Double blueGreenRatio;
    private Double calculatedConcentration;
} 