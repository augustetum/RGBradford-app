package com.rgbradford.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single point on the standard curve
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single data point on the standard curve")
public class StandardCurvePointDto {
    @Schema(
        description = "BSA concentration in mg/mL (y-axis)",
        example = "1.5",
        required = true
    )
    private double concentration; // y
    
    @Schema(
        description = "Average blue-to-green ratio (x-axis)",
        example = "0.75",
        required = true
    )
    private double blueToGreenRatio; // x
}
