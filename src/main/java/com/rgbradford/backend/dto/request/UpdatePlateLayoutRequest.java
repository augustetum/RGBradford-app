package com.rgbradford.backend.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePlateLayoutRequest {
    private int rows;
    private int columns;
} 