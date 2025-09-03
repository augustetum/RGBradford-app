package com.rgbradford.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single point on the standard curve
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandardCurvePointDto {
    /** BSA concentration in mg/mL (x-axis) */
    private double concentration;
    
    /** Average blue-to-green ratio for this concentration (y-axis) */
    private double blueToGreenRatio;
}
