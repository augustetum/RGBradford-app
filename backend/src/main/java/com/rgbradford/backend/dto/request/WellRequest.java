package com.rgbradford.backend.dto.request;

import com.rgbradford.backend.entity.WellType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WellRequest {
    private int row;
    private int column;
    private WellType type;
    private Double standardConcentration;
    private String sampleName;
    private Double dilutionFactor;
    private String replicateGroup;
} 