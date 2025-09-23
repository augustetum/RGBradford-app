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
@Tag(name = "Standard Curve", description = "APIs for managing and calculating standard curves")
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
        summary = "Calculate standard curve",
        description = "Calculates the standard curve based on the standards in the specified plate layout"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully calculated standard curve",
            content = @Content(schema = @Schema(implementation = StandardCurveDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Plate layout not found"
        )
    })
    @GetMapping("/{plateLayoutId}")
    public ResponseEntity<StandardCurveDto> getStandardCurve(
            @Parameter(description = "ID of the plate layout containing standard wells", required = true, example = "1")
            @PathVariable Long plateLayoutId) {
        
        // This will return a cached curve if available, or calculate and store a new one if not
        StandardCurveDto curve = standardCurveService.getStandardCurve(plateLayoutId);
        return ResponseEntity.ok(curve);
    }
    
    @PostMapping("/{plateLayoutId}/recalculate")
    @Operation(summary = "Recalculate standard curve", 
              description = "Forces recalculation of the standard curve for the specified plate layout")
    public ResponseEntity<StandardCurveDto> recalculateStandardCurve(
            @Parameter(description = "ID of the plate layout", required = true, example = "1")
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
        summary = "Get stored standard curve for a project",
        description = "Returns the stored standard curve for the project's plate layout (expects one plate layout per project)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved stored curve",
                     content = @Content(schema = @Schema(implementation = StandardCurveDto.class))),
        @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/proj/{projectId}")
    public ResponseEntity<StandardCurveDto> getProjectStoredStandardCurve(
            @Parameter(description = "ID of the project", required = true, example = "1")
            @PathVariable Long projectId) {
        StandardCurveDto curve = standardCurveService.getStoredStandardCurveByProject(projectId);
        if (curve == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(curve);
    }
}
