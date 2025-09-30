package com.rgbradford.backend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class WellPositionGroupingRequest {
    private List<String> standardWellPositions;
    private List<String> sampleWellPositions;
    private List<String> blankWellPositions;
}
