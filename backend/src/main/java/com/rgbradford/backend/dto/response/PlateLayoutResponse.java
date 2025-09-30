package com.rgbradford.backend.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlateLayoutResponse {
    private Long id;
    private int rows;
    private int columns;
    private Long projectId;
    private int wellCount;
} 