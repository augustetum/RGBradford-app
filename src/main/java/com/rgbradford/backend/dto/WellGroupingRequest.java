package com.rgbradford.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class WellGroupingRequest {
    private List<Long> standardWellIds;
    private List<Long> sampleWellIds;
    private List<Long> blankWellIds;
}
