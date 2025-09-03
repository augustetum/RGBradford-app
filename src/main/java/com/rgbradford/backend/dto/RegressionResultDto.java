package com.rgbradford.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the results of a linear regression analysis
 * where y = slope * x + intercept
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegressionResultDto {
    /** Slope of the best-fit line (m in y = mx + b) */
    private double slope;
    
    /** Y-intercept of the best-fit line (b in y = mx + b) */
    private double intercept;
    
    /** R-squared value indicating the goodness of fit (0-1) */
    private double rSquared;
}
