package com.rgbradford.backend.service.interfaces;

import com.rgbradford.backend.dto.request.PlateAnalysisParams;
import com.rgbradford.backend.dto.response.WellAnalysisResult;

import java.util.List;

public interface PlateAnalysisService {
    List<WellAnalysisResult> analyzePlate(String imagePath, PlateAnalysisParams params) throws Exception;
} 