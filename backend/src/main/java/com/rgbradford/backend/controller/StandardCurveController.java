package com.rgbradford.backend.controller;

import com.rgbradford.backend.dto.StandardCurveDto;
import com.rgbradford.backend.service.interfaces.StandardCurveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
    value = "/api/standard-curve",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
    name = "Standard Curve",
    description = "APIs for calculating and managing standard curves for protein quantitation. " +
                 "Standard curves are generated from wells marked as STANDARD type using linear regression " +
                 "to determine the relationship between blue/green ratios and known concentrations."
)
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class StandardCurveController {

    private final StandardCurveService standardCurveService;

    @Autowired
    public StandardCurveController(StandardCurveService standardCurveService) {
        this.standardCurveService = standardCurveService;
    }

    /**
     * Calculate the standard curve for a given plate layout
     * @param plateLayoutId The ID of the plate layout containing standard wells
     * @return Standard curve data including points and regression statistics
     */
    @Operation(
        summary = "Get or calculate standard curve",
        description = "Retrieves a cached standard curve if available, or calculates a new one based on " +
                "STANDARD type wells in the specified plate layout. The curve uses linear regression " +
                "to fit the relationship between blue/green ratios and known concentrations."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved or calculated standard curve",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = StandardCurveDto.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Standard curve with 8 points",
                    value = """
                        {
                          "plateLayoutId": 1,
                          "slope": 1.2456,
                          "intercept": -0.0234,
                          "rSquared": 0.9987,
                          "points": [
                            {
                              "concentration": 0.125,
                              "blueToGreenRatio": 0.5234,
                              "absorbanceRatio": 0.3456
                            },
                            {
                              "concentration": 0.25,
                              "blueToGreenRatio": 0.7123,
                              "absorbanceRatio": 0.4789
                            },
                            {
                              "concentration": 0.5,
                              "blueToGreenRatio": 0.9876,
                              "absorbanceRatio": 0.6234
                            },
                            {
                              "concentration": 1.0,
                              "blueToGreenRatio": 1.4567,
                              "absorbanceRatio": 0.8123
                            }
                          ],
                          "equation": "y = 1.2456x - 0.0234"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Plate layout not found"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Insufficient standard wells for curve calculation (minimum 2 required)"
        )
    })
    @GetMapping("/{plateLayoutId}")
    public ResponseEntity<StandardCurveDto> getStandardCurve(
            @Parameter(
                description = "ID of the plate layout containing standard wells",
                required = true,
                example = "1"
            )
            @PathVariable Long plateLayoutId) {
        
        // This will return a cached curve if available, or calculate and store a new one if not
        StandardCurveDto curve = standardCurveService.getStandardCurve(plateLayoutId);
        return ResponseEntity.ok(curve);
    }
    
    @Operation(
        summary = "Recalculate standard curve",
        description = "Forces recalculation of the standard curve for the specified plate layout, " +
                "bypassing any cached results. Use this endpoint when standard well data has been updated " +
                "or when you want to ensure the curve reflects the latest analysis results."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully recalculated standard curve",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = StandardCurveDto.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Recalculated standard curve",
                    value = """
                        {
                          "plateLayoutId": 1,
                          "slope": 1.2567,
                          "intercept": -0.0189,
                          "rSquared": 0.9992,
                          "points": [
                            {
                              "concentration": 0.125,
                              "blueToGreenRatio": 0.5312,
                              "absorbanceRatio": 0.3512
                            },
                            {
                              "concentration": 0.25,
                              "blueToGreenRatio": 0.7234,
                              "absorbanceRatio": 0.4823
                            }
                          ],
                          "equation": "y = 1.2567x - 0.0189"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Plate layout not found"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Insufficient standard wells for curve calculation"
        )
    })
    @PostMapping("/{plateLayoutId}/recalculate")
    public ResponseEntity<StandardCurveDto> recalculateStandardCurve(
            @Parameter(
                description = "ID of the plate layout to recalculate",
                required = true,
                example = "1"
            )
            @PathVariable Long plateLayoutId) {
        
        // This will always recalculate and store a new curve
        StandardCurveDto curve = standardCurveService.calculateAndStoreStandardCurve(plateLayoutId);
        return ResponseEntity.ok(curve);
    }

    /**
     * Retrieve the stored standard curve for the project's single plate layout
     * @param projectId The project ID
     * @return Stored standard curve (if exists)
     */
    @Operation(
        summary = "Get stored standard curve by project ID",
        description = "Retrieves the stored standard curve for the project's associated plate layout. " +
                "Since each project has one plate layout, this provides a convenient way to get the curve " +
                "using the project ID instead of the plate layout ID."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved stored curve",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = StandardCurveDto.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                        {
                          "plateLayoutId": 1,
                          "slope": 1.2456,
                          "intercept": -0.0234,
                          "rSquared": 0.9987,
                          "points": [
                            {
                              "concentration": 0.125,
                              "blueToGreenRatio": 0.5234,
                              "absorbanceRatio": 0.3456
                            }
                          ],
                          "equation": "y = 1.2456x - 0.0234"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Project not found or no standard curve available"
        )
    })
    @GetMapping("/proj/{projectId}")
    public ResponseEntity<StandardCurveDto> getProjectStoredStandardCurve(
            @Parameter(
                description = "ID of the project",
                required = true,
                example = "1"
            )
            @PathVariable Long projectId) {
        StandardCurveDto curve = standardCurveService.getStoredStandardCurveByProject(projectId);
        if (curve == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(curve);
    }
}
