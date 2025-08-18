package com.rgbradford.backend.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlateAnalysisParams {
    private int columns;
    private int rows;
    private int xOrigin;
    private int yOrigin;
    private int xEnd;
    private int yEnd;
    private int wellDiameter;
} 