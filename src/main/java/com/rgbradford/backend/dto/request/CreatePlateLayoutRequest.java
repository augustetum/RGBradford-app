package com.rgbradford.backend.dto.request;

import com.rgbradford.backend.entity.Project;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlateLayoutRequest {
    private int rows;
    private int columns;
    private Project project;
} 