package com.rgbradford.backend.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard curve data including points and regression analysis results")
public class StandardCurveDto {
    @ArraySchema(
        schema = @Schema(description = "List of data points used to generate the standard curve"),
        arraySchema = @Schema(description = "Array of standard curve points")
    )
    private List<StandardCurvePointDto> points;
    
    @Schema(description = "Results of the linear regression analysis")
    private RegressionResultDto regression;
}
