package com.rgbradford.backend.service.interfaces;

import com.rgbradford.backend.dto.StandardCurveDto;

public interface StandardCurveService {
    /**
     * Calculates the standard curve based on standard wells in a plate layout.
     * @param plateLayoutId The ID of the plate layout containing standard wells
     * @return StandardCurveDto containing the standard curve data points and statistics
     * @throws IllegalArgumentException if no standard wells are found for the plate layout
     */
    StandardCurveDto calculateStandardCurve(Long plateLayoutId);
}
