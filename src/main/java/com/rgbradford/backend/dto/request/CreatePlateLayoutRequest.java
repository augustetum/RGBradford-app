package com.rgbradford.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlateLayoutRequest {
    @NotNull(message = "Number of rows is required")
    @Min(value = 1, message = "Must have at least 1 row")
    @Max(value = 32, message = "Cannot have more than 32 rows")
    private int rows;
    
    @NotNull(message = "Number of columns is required")
    @Min(value = 1, message = "Must have at least 1 column")
    @Max(value = 24, message = "Cannot have more than 24 columns")
    private int columns;
    
    @NotNull(message = "Project ID is required")
    private Long projectId;
} 