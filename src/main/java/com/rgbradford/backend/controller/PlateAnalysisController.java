package com.rgbradford.backend.controller;

import com.rgbradford.backend.dto.request.PlateAnalysisParams;
import com.rgbradford.backend.dto.response.WellAnalysisResult;
import com.rgbradford.backend.entity.WellAnalysis;
import com.rgbradford.backend.dto.response.WellAnalysisCsvWriter;
import com.rgbradford.backend.service.impl.PlateAnalysisServiceImpl;
import com.rgbradford.backend.repository.WellAnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.DoubleSummaryStatistics;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/plate-analysis")
public class PlateAnalysisController {

    @Autowired
    private PlateAnalysisServiceImpl plateAnalysisService;

    @Autowired
    private WellAnalysisRepository wellAnalysisRepository;

    // POST /api/plate-analysis/analyze
    @PostMapping("/analyze")
    public ResponseEntity<String> analyzePlate(
            @RequestParam("plateLayoutId") Long plateLayoutId,
            @RequestPart("params") PlateAnalysisParams params,
            @RequestPart("image") MultipartFile imageFile
    ) throws Exception {
        
        String uploadDir = "uploads/";
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String imagePath = uploadDir + System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
        imageFile.transferTo(new File(imagePath));

        plateAnalysisService.analyzeAndPersistPlate(plateLayoutId, imagePath, params);

        return ResponseEntity.ok("Analysis complete and results saved.");
    }

    // GET /api/plate-analysis/{plateLayoutId}/csv
    @GetMapping("/{plateLayoutId}/csv")
    public ResponseEntity<byte[]> downloadCsv(@PathVariable Long plateLayoutId) {
        // Fetch all WellAnalysis for the given plateLayoutId using the custom query
        List<WellAnalysis> results = wellAnalysisRepository.findByPlateLayoutId(plateLayoutId);

        String csv = WellAnalysisCsvWriter.toCsvString(results);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plate_" + plateLayoutId + "_results.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }

    // GET /api/plate-analysis/{plateLayoutId} - Get analysis results
    @GetMapping("/{plateLayoutId}")
    public ResponseEntity<List<WellAnalysisResult>> getAnalysisResults(@PathVariable Long plateLayoutId) {
        List<WellAnalysis> results = wellAnalysisRepository.findByPlateLayoutId(plateLayoutId);
        
        if (results.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<WellAnalysisResult> responseResults = results.stream()
                .map(this::convertToWellAnalysisResult)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responseResults);
    }

    // GET /api/plate-analysis/{plateLayoutId}/summary - Get analysis summary
    @GetMapping("/{plateLayoutId}/summary")
    public ResponseEntity<Map<String, Object>> getAnalysisSummary(@PathVariable Long plateLayoutId) {
        List<WellAnalysis> results = wellAnalysisRepository.findByPlateLayoutId(plateLayoutId);
        
        if (results.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalWells", results.size());
        summary.put("plateLayoutId", plateLayoutId);
        
        // Calculate statistics
        DoubleSummaryStatistics concentrationStats = results.stream()
                .filter(wa -> wa.getCalculatedConcentration() != null)
                .mapToDouble(WellAnalysis::getCalculatedConcentration)
                .summaryStatistics();
        
        summary.put("concentrationStats", Map.of(
            "min", concentrationStats.getMin(),
            "max", concentrationStats.getMax(),
            "average", concentrationStats.getAverage(),
            "count", concentrationStats.getCount()
        ));
        
        // Count by well type
        Map<String, Long> wellTypeCounts = results.stream()
                .collect(Collectors.groupingBy(
                    wa -> wa.getWell().getType().toString(),
                    Collectors.counting()
                ));
        summary.put("wellTypeCounts", wellTypeCounts);
        
        return ResponseEntity.ok(summary);
    }

    // DELETE /api/plate-analysis/{plateLayoutId} - Delete analysis results
    @DeleteMapping("/{plateLayoutId}")
    public ResponseEntity<Void> deleteAnalysisResults(@PathVariable Long plateLayoutId) {
        List<WellAnalysis> results = wellAnalysisRepository.findByPlateLayoutId(plateLayoutId);
        
        if (results.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        wellAnalysisRepository.deleteAll(results);
        return ResponseEntity.noContent().build();
    }

    // POST /api/plate-analysis/{plateLayoutId}/reanalyze - Reanalyze with new params
    @PostMapping("/{plateLayoutId}/reanalyze")
    public ResponseEntity<String> reanalyzePlate(
            @PathVariable Long plateLayoutId,
            @RequestPart("params") PlateAnalysisParams params,
            @RequestPart("image") MultipartFile imageFile) throws Exception {
        
        // Delete existing results first
        List<WellAnalysis> existingResults = wellAnalysisRepository.findByPlateLayoutId(plateLayoutId);
        if (!existingResults.isEmpty()) {
            wellAnalysisRepository.deleteAll(existingResults);
        }
        
        // Perform new analysis
        String uploadDir = "uploads/";
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String imagePath = uploadDir + System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
        imageFile.transferTo(new File(imagePath));

        plateAnalysisService.analyzeAndPersistPlate(plateLayoutId, imagePath, params);

        return ResponseEntity.ok("Reanalysis complete and results updated.");
    }

    private WellAnalysisResult convertToWellAnalysisResult(WellAnalysis wellAnalysis) {
        return WellAnalysisResult.builder()
                .id(wellAnalysis.getId())
                .wellId(wellAnalysis.getWell().getId())
                .row(wellAnalysis.getWell().getRow())
                .column(wellAnalysis.getWell().getColumn())
                .wellType(wellAnalysis.getWell().getType().toString())
                .sampleName(wellAnalysis.getWell().getSampleName())
                .greenValue(wellAnalysis.getGreenValue())
                .blueValue(wellAnalysis.getBlueValue())
                .blueToGreenRatio(wellAnalysis.getBlueToGreenRatio())
                .greenAbsorbance(wellAnalysis.getGreenAbsorbance())
                .blueAbsorbance(wellAnalysis.getBlueAbsorbance())
                .absorbanceRatio(wellAnalysis.getAbsorbanceRatio())
                .pixelCount(wellAnalysis.getPixelCount())
                .calculatedConcentration(wellAnalysis.getCalculatedConcentration())
                .build();
    }
} 