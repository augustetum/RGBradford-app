package com.rgbradford.backend.service.interfaces;

import com.rgbradford.backend.dto.request.PlateAnalysisParams;

public interface PlateAnalysisService {
    String analyzePlate(String imagePath, PlateAnalysisParams params) throws Exception;
} 