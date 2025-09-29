package com.rgbradford.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rgbradford.backend.dto.StandardCurveDto;
import com.rgbradford.backend.dto.StandardCurvePointDto;
import com.rgbradford.backend.dto.request.PlateAnalysisParams;
import com.rgbradford.backend.dto.response.WellAnalysisCsvWriter;
import com.rgbradford.backend.dto.response.WellAnalysisResult;
import com.rgbradford.backend.entity.WellAnalysis;
import com.rgbradford.backend.repository.WellAnalysisRepository;
import com.rgbradford.backend.service.impl.PlateAnalysisServiceImpl;
import com.rgbradford.backend.service.interfaces.StandardCurveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.util.DoubleSummaryStatistics;
import java.util.stream.Collectors;

// Apache POI imports for XLSX generation
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@RestController
@RequestMapping("/api/plate-analysis")
public class PlateAnalysisController {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private PlateAnalysisServiceImpl plateAnalysisService;

    @Autowired
    private WellAnalysisRepository wellAnalysisRepository;

    @Autowired
    private StandardCurveService standardCurveService;

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

 @GetMapping(value = "/{plateLayoutId}/xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
public ResponseEntity<byte[]> downloadXlsx(@PathVariable Long plateLayoutId) throws Exception {
    // Gather data: calibration points and all well analyses
    List<WellAnalysis> results = wellAnalysisRepository.findByPlateLayoutId(plateLayoutId);
    StandardCurveDto curve = standardCurveService.getStandardCurve(plateLayoutId);

    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
        // Create styles
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        CellStyle numberStyle = workbook.createCellStyle();
        numberStyle.setDataFormat(workbook.createDataFormat().getFormat("0.0000"));

        // Sheet 1: Calibration Points
        XSSFSheet sheet = workbook.createSheet("Calibration Points");

        // Headers
        String[] headers = {"Blue/Green Ratio (x)", "Concentration (y)"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        List<StandardCurvePointDto> points = curve != null && curve.getPoints() != null ? curve.getPoints() : List.of();
        int rowNum = 1;
        for (StandardCurvePointDto p : points) {
            Row row = sheet.createRow(rowNum++);
            Cell xCell = row.createCell(0);
            xCell.setCellValue(p.getBlueToGreenRatio());
            xCell.setCellStyle(numberStyle);

            Cell yCell = row.createCell(1);
            yCell.setCellValue(p.getConcentration());
            yCell.setCellStyle(numberStyle);
        }

        // Autosize columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        // Sheet 2: Well Analysis (with Dilution Factor columns)
        XSSFSheet wellSheet = workbook.createSheet("Well Analysis");
        String[] wellHeaders = {
                "Row", "Column", "Green", "Blue", "Blue/Green Ratio",
                "Calculated Concentration", "Dilution Factor", "Adjusted Concentration"
        };
        Row wellHeaderRow = wellSheet.createRow(0);
        for (int i = 0; i < wellHeaders.length; i++) {
            Cell hc = wellHeaderRow.createCell(i);
            hc.setCellValue(wellHeaders[i]);
            hc.setCellStyle(headerStyle);
        }

        int wr = 1;
        for (WellAnalysis wa : results) {
            Row row = wellSheet.createRow(wr);
            row.createCell(0).setCellValue(wa.getWell().getRow());
            row.createCell(1).setCellValue(wa.getWell().getColumn());

            Cell cGreen = row.createCell(2);
            cGreen.setCellValue(wa.getGreenValue() != null ? wa.getGreenValue() : 0);

            Cell cBlue = row.createCell(3);
            cBlue.setCellValue(wa.getBlueValue() != null ? wa.getBlueValue() : 0);

            Cell cRatio = row.createCell(4);
            cRatio.setCellValue(wa.getBlueToGreenRatio() != null ? wa.getBlueToGreenRatio() : 0.0);
            cRatio.setCellStyle(numberStyle);

            Cell cCalc = row.createCell(5);
            cCalc.setCellValue(wa.getCalculatedConcentration() != null ? wa.getCalculatedConcentration() : 0.0);
            cCalc.setCellStyle(numberStyle);

            // Column 6 (index 6): Dilution Factor - left blank for user input
            row.createCell(6); // intentionally blank

            // Column 7 (index 7): Adjusted Concentration = Calculated * DilutionFactor (if provided)
            Cell cAdj = row.createCell(7);
            // Excel rows are 1-based; header is row 1, first data row is 2
            int excelRow = wr + 1;
            String formula = String.format("IFERROR(F%d*G%d, \"\")", excelRow, excelRow);
            cAdj.setCellFormula(formula);
            cAdj.setCellStyle(numberStyle);

            wr++;
        }

        for (int i = 0; i < wellHeaders.length; i++) {
            wellSheet.autoSizeColumn(i);
        }

        // Write workbook to bytes
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        byte[] bytes = bos.toByteArray();
        bos.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plate_" + plateLayoutId + "_results.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }
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