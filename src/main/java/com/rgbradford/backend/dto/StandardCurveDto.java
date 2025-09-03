package com.rgbradford.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandardCurveDto {
    private List<StandardCurvePointDto> points; //BSA concentration (x) and corresponding blue-to-green ratio (y)
    private RegressionResultDto regression;  //Results of the linear regression analysis (slope, intercept, and R² value)
}
