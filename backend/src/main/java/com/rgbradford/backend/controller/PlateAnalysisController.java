package com.rgbradford.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rgbradford.backend.dto.StandardCurveDto;
import com.rgbradford.backend.dto.StandardCurvePointDto;
import com.rgbradford.backend.dto.request.PlateAnalysisParams;
import com.rgbradford.backend.dto.response.WellAnalysisCsvWriter;
import com.rgbradford.backend.dto.response.WellAnalysisResult;
import com.rgbradford.backend.entity.WellAnalysis;
import com.rgbradford.backend.entity.WellType;
import com.rgbradford.backend.repository.WellAnalysisRepository;
import com.rgbradford.backend.service.impl.PlateAnalysisServiceImpl;
import com.rgbradford.backend.service.interfaces.StandardCurveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
    name = "Plate Analysis",
    description = "APIs for analyzing plate images, calculating protein concentrations, and exporting results. " +
                 "This controller handles image processing, well detection, RGB analysis, and concentration calculations " +
                 "based on standard curves."
)
@SecurityRequirement(name = "bearerAuth")
public class PlateAnalysisController {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private PlateAnalysisServiceImpl plateAnalysisService;

    @Autowired
    private WellAnalysisRepository wellAnalysisRepository;

    @Autowired
    private StandardCurveService standardCurveService;

    @Operation(
        summary = "Analyze plate image",
        description = """
                Analyzes a plate image to extract RGB values from wells and calculate protein concentrations.

                **This is a multipart/form-data request with three parts:**

                1. **plateLayoutId** (query parameter): The ID of the plate layout (e.g., `1`)

                2. **params** (form field): JSON string with analysis parameters defining the plate grid:
                ```json
                {
                  "columns": 12,
                  "rows": 8,
                  "xOrigin": 100,
                  "yOrigin": 80,
                  "xEnd": 1200,
                  "yEnd": 900,
                  "wellDiameter": 85
                }
                ```

                3. **image** (file upload): Plate image file (JPG, PNG, or TIFF format)

                **Example using cURL:**
                ```bash
                curl -X POST "http://localhost:8080/api/plate-analysis/analyze?plateLayoutId=1" \\
                  -H "Authorization: Bearer YOUR_TOKEN" \\
                  -F 'params={"columns":12,"rows":8,"xOrigin":100,"yOrigin":80,"xEnd":1200,"yEnd":900,"wellDiameter":85}' \\
                  -F "image=@/path/to/plate.jpg"
                ```

                The parameters define the plate grid coordinates and well dimensions for accurate well detection.
                Results are persisted to the database and include RGB values, blue/green ratios, and calculated concentrations.
                """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Analysis completed successfully",
            content = @Content(
                mediaType = "text/plain",
                examples = @ExampleObject(value = "Analysis complete and results saved.")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid parameters or image format"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Plate layout not found"
        )
    })
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> analyzePlate(
            @Parameter(
                description = "ID of the plate layout to analyze",
                required = true,
                example = "1"
            )
            @RequestParam("plateLayoutId") Long plateLayoutId,

            @Parameter(
                description = "Analysis parameters as JSON string",
                required = true,
                example = "{\"columns\":12,\"rows\":8,\"xOrigin\":100,\"yOrigin\":80,\"xEnd\":1200,\"yEnd\":900,\"wellDiameter\":85}"
            )
            @RequestPart("params") String paramsJson,

            @Parameter(
                description = "Plate image file (JPG, PNG, or TIFF format)",
                required = true
            )
            @RequestPart("image") MultipartFile imageFile) throws Exception {
        PlateAnalysisParams params = objectMapper.readValue(paramsJson, PlateAnalysisParams.class);
        plateAnalysisService.analyzeAndPersistPlate(plateLayoutId, imageFile.getInputStream(), params);
        return ResponseEntity.ok("Analysis complete and results saved.");
    }

    @Operation(
        summary = "Reanalyze plate with new parameters or image",
        description = """
                Deletes existing analysis results and performs a new analysis with updated parameters or a different image.

                **This is a multipart/form-data request with three parts:**

                1. **plateLayoutId** (path parameter): The ID of the plate layout to reanalyze (e.g., `1`)

                2. **params** (form field): JSON string with analysis parameters:
                ```json
                {
                  "columns": 12,
                  "rows": 8,
                  "xOrigin": 100,
                  "yOrigin": 80,
                  "xEnd": 1200,
                  "yEnd": 900,
                  "wellDiameter": 85
                }
                ```

                3. **image** (file upload): Plate image file (JPG, PNG, or TIFF format)

                **Example using cURL:**
                ```bash
                curl -X POST "http://localhost:8080/api/plate-analysis/1/reanalyze" \\
                  -H "Authorization: Bearer YOUR_TOKEN" \\
                  -F 'params={"columns":12,"rows":8,"xOrigin":100,"yOrigin":80,"xEnd":1200,"yEnd":900,"wellDiameter":85}' \\
                  -F "image=@/path/to/new_plate.jpg"
                ```

                ⚠️ **Warning:** All previous analysis results for this plate will be permanently deleted before the new analysis begins.
                """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Reanalysis completed successfully",
            content = @Content(
                mediaType = "text/plain",
                examples = @ExampleObject(value = "Reanalysis complete and results updated.")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid parameters or image format"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Plate layout not found"
        )
    })
    @PostMapping(value = "/{plateLayoutId}/reanalyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> reanalyzePlate(
            @Parameter(
                description = "ID of the plate layout to reanalyze",
                required = true,
                example = "1"
            )
            @PathVariable Long plateLayoutId,

            @Parameter(
                description = "Analysis parameters as JSON string",
                required = true,
                example = "{\"columns\":12,\"rows\":8,\"xOrigin\":100,\"yOrigin\":80,\"xEnd\":1200,\"yEnd\":900,\"wellDiameter\":85}"
            )
            @RequestPart("params") String paramsJson,

            @Parameter(
                description = "Plate image file (JPG, PNG, or TIFF format)",
                required = true
            )
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

    @Operation(
        summary = "Download analysis results as CSV",
        description = "Exports well analysis results to a CSV file including well positions, RGB values, " +
                "blue/green ratios, calculated concentrations, and standard curve information."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "CSV file generated successfully",
            content = @Content(
                mediaType = "text/csv"
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No analysis results found for the specified plate layout"
        )
    })
    @GetMapping("/{plateLayoutId}/csv")
    public ResponseEntity<byte[]> downloadCsv(
            @Parameter(
                description = "ID of the plate layout",
                required = true,
                example = "1"
            )
            @PathVariable Long plateLayoutId) {
        // Fetch all WellAnalysis for the given plateLayoutId using the custom query
        List<WellAnalysis> results = wellAnalysisRepository.findByPlateLayoutId(plateLayoutId)
                .stream()
                .filter(wa -> wa.getWell() != null && wa.getWell().getType() != WellType.EMPTY)
                .toList();
        // Fetch calibration curve to include points and enable y=mx+b computation
        StandardCurveDto curve = standardCurveService.getStandardCurve(plateLayoutId);
        String csv = WellAnalysisCsvWriter.toCsvString(results, curve);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plate_" + plateLayoutId + "_results.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }

 @Operation(
        summary = "Download analysis results as Excel (XLSX)",
        description = "Exports well analysis results to an Excel file with two sheets: " +
                "1. 'Calibration Points' - Contains standard curve data points, slope, and intercept calculations. " +
                "2. 'Well Analysis' - Contains individual well measurements with formulas for concentration calculations and dilution factor adjustments. " +
                "The Excel file includes formulas that allow users to manually enter dilution factors and see recalculated concentrations."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Excel file generated successfully",
            content = @Content(
                mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No analysis results found for the specified plate layout"
        )
    })
    @GetMapping(value = "/{plateLayoutId}/xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
public ResponseEntity<byte[]> downloadXlsx(
            @Parameter(
                description = "ID of the plate layout",
                required = true,
                example = "1"
            )
            @PathVariable Long plateLayoutId) throws Exception {
    // Gather data: calibration points and all well analyses (skip EMPTY wells)
    List<WellAnalysis> results = wellAnalysisRepository.findByPlateLayoutId(plateLayoutId)
            .stream()
            .filter(wa -> wa.getWell() != null && wa.getWell().getType() != WellType.EMPTY)
            .toList();
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

        // Compute slope (m) and intercept (b) on the Calibration Points sheet using Excel functions
        // Place labels in C1:D1 and formulas in C2:D2
        Row calcHeader = sheet.createRow(0 + 1); // ensure row 1 exists (already created). We'll use separate cells instead
        // Slope label and value
        sheet.getRow(0).createCell(2).setCellValue("Slope (m)");
        sheet.getRow(0).createCell(3).setCellValue("Intercept (b)");
        // Put formulas into row 2 (index 1)
        Row calcRow = sheet.getRow(1) != null ? sheet.getRow(1) : sheet.createRow(1);
        int lastDataRow = points.size(); // data rows are from 1..points.size()
        String xRange = String.format("A2:A%d", lastDataRow + 1);
        String yRange = String.format("B2:B%d", lastDataRow + 1);
        Cell slopeCell = calcRow.createCell(2);
        slopeCell.setCellFormula(String.format("IF(COUNTA(%s)>=2,SLOPE(%s,%s),\"\")", xRange, yRange, xRange));
        slopeCell.setCellStyle(numberStyle);
        Cell interceptCell = calcRow.createCell(3);
        interceptCell.setCellFormula(String.format("IF(COUNTA(%s)>=2,INTERCEPT(%s,%s),\"\")", xRange, yRange, xRange));
        interceptCell.setCellStyle(numberStyle);

        // Sheet 2: Well Analysis (with Dilution Factor columns)
        XSSFSheet wellSheet = workbook.createSheet("Well Analysis");
        String[] wellHeaders = {
                "Row", "Column", "Green", "Blue", "Blue/Green Ratio",
                "Calculated Concentration (m*x+b)", "Dilution Factor", "Adjusted Concentration"
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
            // Formula: m*x + b using slope/intercept from Calibration Points!C2 and D2
            int excelRow = wr + 1; // current wellSheet row number in Excel
            String calcFormula = String.format("IFERROR('Calibration Points'!$C$2*E%d + 'Calibration Points'!$D$2, \"\")", excelRow);
            cCalc.setCellFormula(calcFormula);
            cCalc.setCellStyle(numberStyle);

            // Column 6 (index 6): Dilution Factor - left blank for user input
            row.createCell(6); // intentionally blank

            // Column 7 (index 7): Adjusted Concentration = Calculated * DilutionFactor (if provided)
            Cell cAdj = row.createCell(7);
            String adjFormula = String.format("IFERROR(F%d*G%d, \"\")", excelRow, excelRow);
            cAdj.setCellFormula(adjFormula);
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

    @Operation(
        summary = "Get analysis results for a plate",
        description = "Retrieves all well analysis results for the specified plate layout. " +
                "Returns detailed information including RGB values, ratios, absorbances, and calculated concentrations. " +
                "Empty wells are excluded from the results."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved analysis results",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WellAnalysisResult.class),
                examples = @ExampleObject(
                    name = "Analysis results example",
                    value = """
                        [
                          {
                            "id": 1,
                            "wellId": 5,
                            "row": 0,
                            "column": 0,
                            "wellType": "STANDARD",
                            "sampleName": null,
                            "greenValue": 145,
                            "blueValue": 89,
                            "blueToGreenRatio": 0.6138,
                            "calculatedConcentration": 0.5,
                            "greenAbsorbance": 0.8382,
                            "blueAbsorbance": 0.5501,
                            "absorbanceRatio": 0.6563,
                            "pixelCount": 5432
                          },
                          {
                            "id": 2,
                            "wellId": 6,
                            "row": 0,
                            "column": 1,
                            "wellType": "SAMPLE",
                            "sampleName": "Sample A1",
                            "greenValue": 122,
                            "blueValue": 156,
                            "blueToGreenRatio": 1.2787,
                            "calculatedConcentration": 1.87,
                            "greenAbsorbance": 0.9136,
                            "blueAbsorbance": 0.8069,
                            "absorbanceRatio": 0.8832,
                            "pixelCount": 5398
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No analysis results found for the specified plate layout"
        )
    })
    @GetMapping("/{plateLayoutId}")
    public ResponseEntity<List<WellAnalysisResult>> getAnalysisResults(
            @Parameter(
                description = "ID of the plate layout",
                required = true,
                example = "1"
            )
            @PathVariable Long plateLayoutId) {
        List<WellAnalysis> results = wellAnalysisRepository.findByPlateLayoutId(plateLayoutId)
                .stream()
                .filter(wa -> wa.getWell() != null && wa.getWell().getType() != WellType.EMPTY)
                .toList();

        if (results.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<WellAnalysisResult> responseResults = results.stream()
                .map(this::convertToWellAnalysisResult)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseResults);
    }

    @Operation(
        summary = "Get analysis summary statistics",
        description = "Retrieves statistical summary of the analysis results including total well count, " +
                "concentration statistics (min, max, average), and well type distribution. " +
                "Useful for quality control and quick overview of plate analysis."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved analysis summary",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Summary statistics example",
                    value = """
                        {
                          "totalWells": 96,
                          "plateLayoutId": 1,
                          "concentrationStats": {
                            "min": 0.125,
                            "max": 2.5,
                            "average": 1.2375,
                            "count": 88
                          },
                          "wellTypeCounts": {
                            "STANDARD": 8,
                            "SAMPLE": 72,
                            "CONTROL": 8,
                            "BLANK": 8
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No analysis results found for the specified plate layout"
        )
    })
    @GetMapping("/{plateLayoutId}/summary")
    public ResponseEntity<Map<String, Object>> getAnalysisSummary(
            @Parameter(
                description = "ID of the plate layout",
                required = true,
                example = "1"
            )
            @PathVariable Long plateLayoutId) {
        List<WellAnalysis> results = wellAnalysisRepository.findByPlateLayoutId(plateLayoutId)
                .stream()
                .filter(wa -> wa.getWell() != null && wa.getWell().getType() != WellType.EMPTY)
                .toList();

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

    @Operation(
        summary = "Delete analysis results",
        description = "Permanently deletes all analysis results for the specified plate layout. " +
                "This action cannot be undone. The plate layout itself is not deleted, only the analysis data."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Analysis results successfully deleted"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No analysis results found for the specified plate layout"
        )
    })
    @DeleteMapping("/{plateLayoutId}")
    public ResponseEntity<Void> deleteAnalysisResults(
            @Parameter(
                description = "ID of the plate layout",
                required = true,
                example = "1"
            )
            @PathVariable Long plateLayoutId) {
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