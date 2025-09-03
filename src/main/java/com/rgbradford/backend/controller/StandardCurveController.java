package com.rgbradford.backend.controller;

import com.rgbradford.backend.dto.StandardCurveDto;
import com.rgbradford.backend.service.interfaces.StandardCurveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/standard-curve")
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
    @GetMapping("/{plateLayoutId}")
    public ResponseEntity<StandardCurveDto> getStandardCurve(
            @PathVariable Long plateLayoutId) {
        
        StandardCurveDto curve = standardCurveService.calculateStandardCurve(plateLayoutId);
        return ResponseEntity.ok(curve);
    }
}
