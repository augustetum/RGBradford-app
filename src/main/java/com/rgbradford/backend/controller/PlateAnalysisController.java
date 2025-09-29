package com.rgbradford.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rgbradford.backend.dto.request.PlateAnalysisParams;
import com.rgbradford.backend.dto.response.WellAnalysisCsvWriter;
import com.rgbradford.backend.dto.response.WellAnalysisResult;
import com.rgbradford.backend.entity.WellAnalysis;
import com.rgbradford.backend.repository.WellAnalysisRepository;
import com.rgbradford.backend.service.impl.PlateAnalysisServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/plate-analysis")
public class PlateAnalysisController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private PlateAnalysisServiceImpl plateAnalysisService;

    @Autowired
    private WellAnalysisRepository wellAnalysisRepository;

    @PostMapping("/analyze")
    public ResponseEntity<String> analyzePlate(
            @RequestParam("plateLayoutId") Long plateLayoutId,
            @RequestPart("params") String paramsJson,
            @RequestPart("image") MultipartFile imageFile) throws Exception {
        PlateAnalysisParams params = objectMapper.readValue(paramsJson, PlateAnalysisParams.class);
        plateAnalysisService.analyzeAndPersistPlate(plateLayoutId, imageFile.getInputStream(), params);
        return ResponseEntity.ok("Analysis complete and results saved.");
    }

    @PostMapping("/{plateLayoutId}/reanalyze")
    public ResponseEntity<String> reanalyzePlate(
            @PathVariable Long plateLayoutId,
            @RequestPart("params") String paramsJson,
            @RequestPart("image") MultipartFile imageFile) throws Exception {

        //deletes existing results
        List<WellAnalysis> existingResults = wellAnalysisRepository.findByPlateLayoutId(plateLayoutId);
        if (!existingResults.isEmpty()) {
            wellAnalysisRepository.deleteAll(existingResults);
        }

        PlateAnalysisParams params = objectMapper.readValue(paramsJson, PlateAnalysisParams.class);
        plateAnalysisService.analyzeAndPersistPlate(plateLayoutId, imageFile.getInputStream(), params);

        return ResponseEntity.ok("Reanalysis complete and results updated.");
    }

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

    @GetMapping("/{plateLayoutId}/summary")
    public ResponseEntity<Map<String, Object>> getAnalysisSummary(@PathVariable Long plateLayoutId) {
        List<WellAnalysis> results = wellAnalysisRepository.findByPlateLayoutId(plateLayoutId);

        if (results.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalWells", results.size());
        summary.put("plateLayoutId", plateLayoutId);

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

        //counts by well type
        Map<String, Long> wellTypeCounts = results.stream()
                .collect(Collectors.groupingBy(
                        wa -> wa.getWell().getType().toString(),
                        Collectors.counting()
                ));
        summary.put("wellTypeCounts", wellTypeCounts);

        return ResponseEntity.ok(summary);
    }

    @DeleteMapping("/{plateLayoutId}")
    public ResponseEntity<Void> deleteAnalysisResults(@PathVariable Long plateLayoutId) {
        List<WellAnalysis> results = wellAnalysisRepository.findByPlateLayoutId(plateLayoutId);

        if (results.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        wellAnalysisRepository.deleteAll(results);
        return ResponseEntity.noContent().build();
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