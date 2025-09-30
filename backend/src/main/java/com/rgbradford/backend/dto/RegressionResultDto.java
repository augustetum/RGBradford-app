package com.rgbradford.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Results of the linear regression analysis (y = slope * x + intercept)")
public class RegressionResultDto {
    @Schema(
        description = "Slope of the best-fit line (m in y = mx + b)",
        example = "0.5",
        required = true
    )
    private double slope;
    
    @Schema(
        description = "Y-intercept of the best-fit line (b in y = mx + b)",
        example = "0.1",
        required = true
    )
    private double intercept;
    
    @Schema(
        description = "R-squared value indicating the goodness of fit (0-1)",
        example = "0.98",
        minimum = "0",
        maximum = "1",
        required = true
    )
    private double rSquared;
}
