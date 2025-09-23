package com.rgbradford.backend.service.interfaces;

import com.rgbradford.backend.dto.StandardCurveDto;
import java.util.List;

public interface StandardCurveService {
    /**
     * Gets the standard curve for a plate layout, either from cache/database or by calculating it.
     * @param plateLayoutId The ID of the plate layout containing standard wells
     * @return StandardCurveDto containing the standard curve data points and statistics
     * @throws IllegalArgumentException if no standard wells are found for the plate layout
     */
    StandardCurveDto getStandardCurve(Long plateLayoutId);
    
    /**
     * Calculates the standard curve based on standard wells in a plate layout and stores it.
     * @param plateLayoutId The ID of the plate layout containing standard wells
     * @return StandardCurveDto containing the standard curve data points and statistics
     * @throws IllegalArgumentException if no standard wells are found for the plate layout
     */
    StandardCurveDto calculateAndStoreStandardCurve(Long plateLayoutId);
    
    /**
     * Retrieves a previously calculated standard curve for a plate layout.
     * @param plateLayoutId The ID of the plate layout
     * @return StandardCurveDto if found, or null if not found
     */
    StandardCurveDto getStoredStandardCurve(Long plateLayoutId);

    /**
     * Retrieves all previously calculated standard curves for a project (by its plate layouts).
     * @param projectId The ID of the project
     * @return List of StandardCurveDto for all plate layouts that have a stored curve
     */
    List<StandardCurveDto> getStoredStandardCurvesByProject(Long projectId);

    /**
     * Retrieves a single stored standard curve for a project. Intended for projects with exactly one plate layout.
     * @param projectId The ID of the project
     * @return StandardCurveDto if found, or null if not found
     */
    StandardCurveDto getStoredStandardCurveByProject(Long projectId);
}
