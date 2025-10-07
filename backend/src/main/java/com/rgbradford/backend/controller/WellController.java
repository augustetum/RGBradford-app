package com.rgbradford.backend.controller;

import com.rgbradford.backend.dto.request.WellRequest;
import com.rgbradford.backend.dto.response.WellResponse;
import com.rgbradford.backend.entity.Well;
import com.rgbradford.backend.entity.WellType;
import com.rgbradford.backend.repository.WellRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wells")
@Tag(
    name = "Well",
    description = "APIs for managing individual wells within plate layouts. " +
                 "Wells can be configured as STANDARD, SAMPLE, CONTROL, BLANK, or EMPTY types. " +
                 "Each well stores metadata such as concentration, sample name, dilution factor, and replicate group."
)
@SecurityRequirement(name = "bearerAuth")
public class WellController {

    @Autowired
    private WellRepository wellRepository;

    @Operation(
        summary = "List all wells with optional filters",
        description = "Retrieves a paginated list of wells. Supports filtering by plate layout, well type, or replicate group."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved wells",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "content": [
                            {
                              "id": 1,
                              "row": 0,
                              "column": 0,
                              "position": "A1",
                              "type": "STANDARD",
                              "standardConcentration": 0.5,
                              "sampleName": null,
                              "dilutionFactor": 1.0,
                              "replicateGroup": "STD1",
                              "plateLayoutId": 1
                            },
                            {
                              "id": 2,
                              "row": 0,
                              "column": 1,
                              "position": "A2",
                              "type": "SAMPLE",
                              "standardConcentration": null,
                              "sampleName": "Sample A",
                              "dilutionFactor": 10.0,
                              "replicateGroup": "G1",
                              "plateLayoutId": 1
                            }
                          ],
                          "totalElements": 96,
                          "totalPages": 5
                        }
                        """
                )
            )
        )
    })
    @GetMapping
    public ResponseEntity<Page<WellResponse>> getAllWells(
            @Parameter(description = "Filter by plate layout ID", example = "1")
            @RequestParam(required = false) Long plateLayoutId,
            @Parameter(description = "Filter by well type (STANDARD, SAMPLE, CONTROL, BLANK, EMPTY)")
            @RequestParam(required = false) WellType wellType,
            @Parameter(description = "Filter by replicate group name", example = "G1")
            @RequestParam(required = false) String replicateGroup,
            @Parameter(hidden = true) Pageable pageable) {
        
        Page<Well> wells;
        if (plateLayoutId != null) {
            wells = wellRepository.findByPlateLayoutId(plateLayoutId, pageable);
        } else if (wellType != null) {
            wells = wellRepository.findByType(wellType, pageable);
        } else if (replicateGroup != null) {
            wells = wellRepository.findByReplicateGroup(replicateGroup, pageable);
        } else {
            wells = wellRepository.findAll(pageable);
        }
        
        Page<WellResponse> responses = wells.map(this::convertToResponse);
        return ResponseEntity.ok(responses);
    }

    @Operation(
        summary = "Get well by ID",
        description = "Retrieves detailed information about a specific well including its position, type, and metadata."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved well",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WellResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Well not found"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<WellResponse> getWell(
            @Parameter(description = "ID of the well", required = true, example = "1")
            @PathVariable Long id) {
        return wellRepository.findById(id)
                .map(this::convertToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Create a new well",
        description = "Creates a new well with the specified configuration. Note: It's recommended to use the batch well creation " +
                "endpoint in PlateLayoutController for better performance when creating multiple wells."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Well successfully created",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WellResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request"
        )
    })
    @PostMapping
    public ResponseEntity<WellResponse> createWell(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Well configuration",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = WellRequest.class),
                    examples = @ExampleObject(
                        value = """
                            {
                              "row": 0,
                              "column": 0,
                              "type": "STANDARD",
                              "standardConcentration": 0.5,
                              "sampleName": null,
                              "dilutionFactor": 1.0,
                              "replicateGroup": "STD1"
                            }
                            """
                    )
                )
            )
            @RequestBody WellRequest request) {
        Well well = Well.builder()
                .row(request.getRow())
                .column(request.getColumn())
                .type(request.getType())
                .standardConcentration(request.getStandardConcentration())
                .sampleName(request.getSampleName())
                .dilutionFactor(request.getDilutionFactor())
                .replicateGroup(request.getReplicateGroup())
                .build();
        
        Well saved = wellRepository.save(well);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponse(saved));
    }

    @Operation(
        summary = "Update well",
        description = "Updates an existing well's configuration including type, concentration, sample name, and other metadata."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Well successfully updated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WellResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Well not found"
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<WellResponse> updateWell(
            @Parameter(description = "ID of the well to update", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Updated well configuration",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = WellRequest.class),
                    examples = @ExampleObject(
                        value = """
                            {
                              "row": 0,
                              "column": 0,
                              "type": "SAMPLE",
                              "standardConcentration": null,
                              "sampleName": "Updated Sample",
                              "dilutionFactor": 5.0,
                              "replicateGroup": "G2"
                            }
                            """
                    )
                )
            )
            @RequestBody WellRequest request) {
        
        return wellRepository.findById(id)
                .map(well -> {
                    well.setRow(request.getRow());
                    well.setColumn(request.getColumn());
                    well.setType(request.getType());
                    well.setStandardConcentration(request.getStandardConcentration());
                    well.setSampleName(request.getSampleName());
                    well.setDilutionFactor(request.getDilutionFactor());
                    well.setReplicateGroup(request.getReplicateGroup());
                    
                    Well saved = wellRepository.save(well);
                    return ResponseEntity.ok(convertToResponse(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Delete well",
        description = "Permanently deletes a well. This action cannot be undone."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Well successfully deleted"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Well not found"
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWell(
            @Parameter(description = "ID of the well to delete", required = true, example = "1")
            @PathVariable Long id) {
        if (!wellRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        wellRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Get all wells for a plate layout",
        description = "Retrieves all wells belonging to a specific plate layout (non-paginated)."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved wells"
        )
    })
    @GetMapping("/plate/{plateLayoutId}")
    public ResponseEntity<List<WellResponse>> getWellsByPlate(
            @Parameter(description = "ID of the plate layout", required = true, example = "1")
            @PathVariable Long plateLayoutId) {
        List<Well> wells = wellRepository.findByPlateLayoutId(plateLayoutId);
        List<WellResponse> responses = wells.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @Operation(
        summary = "Get wells by type",
        description = "Retrieves all wells of a specific type (STANDARD, SAMPLE, CONTROL, BLANK, or EMPTY) with pagination."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved wells"
        )
    })
    @GetMapping("/type/{wellType}")
    public ResponseEntity<Page<WellResponse>> getWellsByType(
            @Parameter(
                description = "Type of wells to retrieve",
                required = true,
                example = "STANDARD",
                schema = @Schema(allowableValues = {"STANDARD", "SAMPLE", "CONTROL", "BLANK", "EMPTY"})
            )
            @PathVariable WellType wellType,
            @Parameter(hidden = true) Pageable pageable) {
        
        Page<Well> wells = wellRepository.findByType(wellType, pageable);
        Page<WellResponse> responses = wells.map(this::convertToResponse);
        
        return ResponseEntity.ok(responses);
    }

    @Operation(
        summary = "Get wells by replicate group",
        description = "Retrieves all wells belonging to a specific replicate group with pagination. " +
                "Replicate groups are used to identify technical or biological replicates of the same sample."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved wells"
        )
    })
    @GetMapping("/replicate/{group}")
    public ResponseEntity<Page<WellResponse>> getWellsByReplicateGroup(
            @Parameter(description = "Name of the replicate group", required = true, example = "G1")
            @PathVariable String group,
            @Parameter(hidden = true) Pageable pageable) {
        
        Page<Well> wells = wellRepository.findByReplicateGroup(group, pageable);
        Page<WellResponse> responses = wells.map(this::convertToResponse);
        
        return ResponseEntity.ok(responses);
    }

    private WellResponse convertToResponse(Well well) {
        return WellResponse.builder()
                .id(well.getId())
                .row(well.getRow())
                .column(well.getColumn())
                .type(well.getType())
                .standardConcentration(well.getStandardConcentration())
                .sampleName(well.getSampleName())
                .dilutionFactor(well.getDilutionFactor())
                .replicateGroup(well.getReplicateGroup())
                .plateLayoutId(well.getPlateLayout().getId())
                .build();
    }
} 