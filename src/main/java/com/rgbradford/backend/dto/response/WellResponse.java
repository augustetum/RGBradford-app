package com.rgbradford.backend.dto.response;

import com.rgbradford.backend.entity.WellType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WellResponse {
    private Long id;
    private int row;
    private int column;
    private WellType type;
    private Double standardConcentration;
    private String sampleName;
    private Double dilutionFactor;
    private String replicateGroup;
    private Long plateLayoutId;
} 