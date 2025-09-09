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
        
        StandardCurveDto curve = standardCurveService.calculateStandardCurve(plateLayoutId);
        return ResponseEntity.ok(curve);
    }
}
