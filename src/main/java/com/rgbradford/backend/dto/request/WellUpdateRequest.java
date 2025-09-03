package com.rgbradford.backend.dto.request;

import com.rgbradford.backend.entity.WellType;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class WellUpdateRequest {
    @NotNull
    private String position;  // e.g., "A1", "B2"
    
    private WellType type;
    private Double standardConcentration;  // For standard wells
    private String sampleName;            // For sample wells
    private Double dilutionFactor;        // For sample wells
    private String replicateGroup;        // For grouping wells
}
