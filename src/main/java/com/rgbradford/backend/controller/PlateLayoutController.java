package com.rgbradford.backend.controller;

import com.rgbradford.backend.dto.request.CreatePlateLayoutRequest;
import com.rgbradford.backend.dto.request.UpdatePlateLayoutRequest;
import com.rgbradford.backend.dto.request.WellRequest;
import com.rgbradford.backend.dto.request.WellPositionGroupingRequest;
import com.rgbradford.backend.dto.request.WellUpdateRequest;
import com.rgbradford.backend.dto.response.PlateLayoutResponse;
import com.rgbradford.backend.dto.response.WellResponse;
import com.rgbradford.backend.dto.WellGroupingRequest;
import com.rgbradford.backend.entity.PlateLayout;
import com.rgbradford.backend.entity.Project;
import com.rgbradford.backend.entity.Well;
import com.rgbradford.backend.entity.WellType;
import com.rgbradford.backend.util.WellPositionUtils;
import com.rgbradford.backend.repository.PlateLayoutRepository;
import com.rgbradford.backend.repository.ProjectRepository;
import com.rgbradford.backend.repository.WellRepository;
import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/plate-layouts")
@Tag(name = "Plate Layout", description = "APIs for managing plate layouts and wells")
public class PlateLayoutController {

    @Autowired
    private PlateLayoutRepository plateLayoutRepository;

    @Autowired
    private WellRepository wellRepository;
    
    @Autowired
    private ProjectRepository projectRepository;

    // GET /api/plate-layouts - List all plate layouts
    @GetMapping
    public ResponseEntity<Page<PlateLayoutResponse>> getAllPlateLayouts(Pageable pageable) {
        Page<PlateLayout> plateLayouts = plateLayoutRepository.findAll(pageable);
        Page<PlateLayoutResponse> responses = plateLayouts.map(this::convertToResponse);
        return ResponseEntity.ok(responses);
    }

    // GET /api/plate-layouts/{id} - Get specific plate layout
    @GetMapping("/{id}")
    public ResponseEntity<PlateLayoutResponse> getPlateLayout(@PathVariable Long id) {
        return plateLayoutRepository.findById(id)
                .map(this::convertToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/plate-layouts - Create new plate layout
    @PostMapping
    @Transactional
    public ResponseEntity<PlateLayoutResponse> createPlateLayout(
            @Valid @RequestBody CreatePlateLayoutRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // Get the authenticated user's email
            String email = userDetails.getUsername();
            
            // Find the project and verify ownership
            Project project = projectRepository.findByIdAndUser_Email(
                request.getProjectId(), 
                email
            ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Project not found with id: " + request.getProjectId() + " or you don't have access to it"
            ));
            
            // Check if project already has a plate layout
            if (project.getPlateLayout() != null) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "Project already has a plate layout"
                );
            }
            
            // Create the plate layout
            PlateLayout plateLayout = new PlateLayout();
            plateLayout.setRows(request.getRows());
            plateLayout.setColumns(request.getColumns());
            
            // Set the bidirectional relationship
            plateLayout.setProject(project);
            
            // Save the plate layout
            PlateLayout savedPlateLayout = plateLayoutRepository.save(plateLayout);
            
            // Return the created plate layout
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(convertToResponse(savedPlateLayout));
                
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error creating plate layout: " + e.getMessage(), 
                e
            );
        }
    }

    @PostMapping("/{id}/init-wells")
    @Transactional
    public ResponseEntity<List<WellResponse>> initializeWells(@PathVariable Long id) {
        return plateLayoutRepository.findById(id)
                .map(plateLayout -> {
                    // Delete any existing wells first
                    wellRepository.deleteByPlateLayoutId(id);
                    
                    List<Well> wells = new ArrayList<>();
                    
                    // Create wells for each position in the plate
                    for (int row = 0; row < plateLayout.getRows(); row++) {
                        for (int col = 0; col < plateLayout.getColumns(); col++) {
                            String position = WellPositionUtils.toPosition(row, col);
                            Well well = Well.builder()
                                    .row(row)
                                    .column(col)
                                    .position(position)
                                    .type(WellType.EMPTY)
                                    .plateLayout(plateLayout)
                                    .build();
                            wells.add(well);
                        }
                    }
                
                // Save all wells
                List<Well> savedWells = wellRepository.saveAll(wells);
                
                // Convert to response
                List<WellResponse> responses = savedWells.stream()
                        .map(this::convertToWellResponse)
                        .collect(Collectors.toList());
                
                return ResponseEntity.ok(responses);
            })
            .orElse(ResponseEntity.notFound().build());
}

    // PUT /api/plate-layouts/{id} - Update plate layout
    @PutMapping("/{id}")
    public ResponseEntity<PlateLayoutResponse> updatePlateLayout(
            @PathVariable Long id, 
            @RequestBody UpdatePlateLayoutRequest request) {
        
        return plateLayoutRepository.findById(id)
                .map(plateLayout -> {
                    plateLayout.setRows(request.getRows());
                    plateLayout.setColumns(request.getColumns());
                    PlateLayout saved = plateLayoutRepository.save(plateLayout);
                    return ResponseEntity.ok(convertToResponse(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/plate-layouts/{id} - Delete plate layout
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlateLayout(@PathVariable Long id) {
        if (!plateLayoutRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        plateLayoutRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/plate-layouts/{id}/wells - Get all wells for a plate
    @GetMapping("/{id}/wells")
    public ResponseEntity<List<WellResponse>> getWellsForPlate(@PathVariable Long id) {
        if (!plateLayoutRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        List<Well> wells = wellRepository.findByPlateLayoutId(id);
        List<WellResponse> responses = wells.stream()
                .map(this::convertToWellResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    // POST /api/plate-layouts/{id}/wells - Add wells to plate layout
    @PostMapping("/{id}/wells")
    public ResponseEntity<List<WellResponse>> addWellsToPlate(
            @PathVariable Long id, 
            @RequestBody List<WellRequest> wellRequests) {
        
        return plateLayoutRepository.findById(id)
                .map(plateLayout -> {
                    List<Well> wells = wellRequests.stream()
                            .map(request -> {
                                String position = WellPositionUtils.toPosition(request.getRow(), request.getColumn());
                                return Well.builder()
                                    .row(request.getRow())
                                    .column(request.getColumn())
                                    .position(position)
                                    .type(request.getType())
                                    .standardConcentration(request.getStandardConcentration())
                                    .sampleName(request.getSampleName())
                                    .dilutionFactor(request.getDilutionFactor())
                                    .replicateGroup(request.getReplicateGroup())
                                    .plateLayout(plateLayout)
                                    .build();
                            })
                            .collect(Collectors.toList());
                    
                    List<Well> savedWells = wellRepository.saveAll(wells);
                    List<WellResponse> responses = savedWells.stream()
                            .map(this::convertToWellResponse)
                            .collect(Collectors.toList());
                    
                    return ResponseEntity.status(HttpStatus.CREATED).body(responses);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/plate-layouts/{id}/wells - Update wells in plate layout using position (A1, B2, etc.)
    @PutMapping("/{id}/wells")
    @Transactional
    @Operation(summary = "Update multiple wells in a plate layout",
               description = "Update or create wells using their position (e.g., 'A1', 'B2'). Only updates fields that are provided in the request.")
    public ResponseEntity<List<WellResponse>> updateWellsInPlate(
            @PathVariable Long id, 
            @Valid @RequestBody List<WellUpdateRequest> wellUpdateRequests) {
        
        return plateLayoutRepository.findById(id)
                .map(plateLayout -> {
                    // Get all existing wells for this plate
                    Map<String, Well> existingWells = wellRepository.findByPlateLayoutId(id).stream()
                            .collect(Collectors.toMap(Well::getPosition, Function.identity()));
                    
                    List<Well> wellsToSave = new ArrayList<>();
                    
                    // Process each well in the request
                    for (WellUpdateRequest request : wellUpdateRequests) {
                        String position = request.getPosition().toUpperCase();
                        
                        // Get existing well or create a new one
                        Well well = existingWells.getOrDefault(position, new Well());
                        
                        // Update position and parse row/column from it
                        well.setPosition(position);
                        try {
                            int[] rowCol = WellPositionUtils.fromPosition(position);
                            well.setRow(rowCol[0]);
                            well.setColumn(rowCol[1]);
                        } catch (IllegalArgumentException e) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                                "Invalid position format: " + position + ". Expected format like 'A1', 'B2', etc.");
                        }
                        
                        // Update other fields if provided
                        if (request.getType() != null) {
                            well.setType(request.getType());
                        }
                        if (request.getStandardConcentration() != null) {
                            well.setStandardConcentration(request.getStandardConcentration());
                        }
                        if (request.getSampleName() != null) {
                            well.setSampleName(request.getSampleName());
                        }
                        if (request.getDilutionFactor() != null) {
                            well.setDilutionFactor(request.getDilutionFactor());
                        }
                        if (request.getReplicateGroup() != null) {
                            well.setReplicateGroup(request.getReplicateGroup());
                        }
                        
                        well.setPlateLayout(plateLayout);
                        wellsToSave.add(well);
                    }
                    
                    // Save all wells (both updates and new ones)
                    List<Well> savedWells = wellRepository.saveAll(wellsToSave);
                    
                    // Return the updated wells
                    List<WellResponse> responses = savedWells.stream()
                            .map(this::convertToWellResponse)
                            .collect(Collectors.toList());
                    
                    return ResponseEntity.ok(responses);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/plate-layouts/{id}/group-wells - Group wells into standard, sample, and blank
    @PostMapping("/{id}/group-wells")
    @Deprecated
    public ResponseEntity<List<WellResponse>> groupWells(
            @PathVariable Long id,
            @RequestBody WellGroupingRequest request) {

        return plateLayoutRepository.findById(id)
                .map(plateLayout -> {
                    List<Well> wells = wellRepository.findByPlateLayoutId(id);
                    Map<Long, Well> wellMap = wells.stream().collect(Collectors.toMap(Well::getId, Function.identity()));

                    updateWellTypes(wellMap, request.getStandardWellIds(), WellType.STANDARD);
                    updateWellTypes(wellMap, request.getSampleWellIds(), WellType.SAMPLE);
                    updateWellTypes(wellMap, request.getBlankWellIds(), WellType.BLANK);

                    Set<Long> groupedWellIds = Stream.of(
                            request.getStandardWellIds(),
                            request.getSampleWellIds(),
                            request.getBlankWellIds())
                            .flatMap(List::stream)
                            .collect(Collectors.toSet());

                    wells.forEach(well -> {
                        if (!groupedWellIds.contains(well.getId())) {
                            well.setType(WellType.EMPTY);
                        }
                    });

                    List<Well> savedWells = wellRepository.saveAll(wells);
                    List<WellResponse> responses = savedWells.stream()
                            .map(this::convertToWellResponse)
                            .collect(Collectors.toList());

                    return ResponseEntity.ok(responses);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Group wells by their position (e.g., "A1", "B2") instead of well IDs
     */
    @PostMapping("/{id}/group-wells-by-position")
    @Operation(summary = "Group wells by position into standard, sample, and blank wells")
    public ResponseEntity<List<WellResponse>> groupWellsByPosition(
            @Parameter(description = "ID of the plate layout") @PathVariable Long id,
            @RequestBody WellPositionGroupingRequest request) {
            
        return plateLayoutRepository.findById(id)
                .map(plateLayout -> {
                    // Get all wells for this plate
                    List<Well> wells = wellRepository.findByPlateLayoutId(id);
                    
                    // Create a map of position to well for quick lookup
                    Map<String, Well> positionToWellMap = wells.stream()
                            .collect(Collectors.toMap(
                                    well -> WellPositionUtils.toPosition(well.getRow(), well.getColumn()),
                                    Function.identity()
                            ));
                    
                    // Update well types based on position
                    updateWellTypesByPosition(positionToWellMap, request.getStandardWellPositions(), WellType.STANDARD);
                    updateWellTypesByPosition(positionToWellMap, request.getSampleWellPositions(), WellType.SAMPLE);
                    updateWellTypesByPosition(positionToWellMap, request.getBlankWellPositions(), WellType.BLANK);
                    
                    // Mark all other wells as EMPTY
                    Set<String> groupedPositions = new HashSet<>();
                    
                    // Helper method to add positions to the set
                    Consumer<List<String>> addPositions = (List<String> positions) -> {
                        if (positions != null) {
                            positions.stream()
                                .map(String::toUpperCase)
                                .forEach(groupedPositions::add);
                        }
                    };
                    
                    // Add all positions from each list
                    addPositions.accept(request.getStandardWellPositions());
                    addPositions.accept(request.getSampleWellPositions());
                    addPositions.accept(request.getBlankWellPositions());
                            
                    positionToWellMap.forEach((position, well) -> {
                        if (!groupedPositions.contains(position)) {
                            well.setType(WellType.EMPTY);
                        }
                    });
                    
                    // Save all changes
                    List<Well> savedWells = wellRepository.saveAll(wells);
                    
                    // Convert to response
                    List<WellResponse> responses = savedWells.stream()
                            .map(this::convertToWellResponse)
                            .collect(Collectors.toList());
                            
                    return ResponseEntity.ok(responses);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    private void updateWellTypesByPosition(Map<String, Well> positionToWellMap, List<String> positions, WellType wellType) {
        if (positions != null) {
            positions.stream()
                    .map(String::toUpperCase) // Ensure case-insensitive matching
                    .forEach(position -> {
                        if (positionToWellMap.containsKey(position)) {
                            positionToWellMap.get(position).setType(wellType);
                        } else {
                            // Log a warning if position doesn't exist
                            System.out.println("Warning: Position " + position + " not found in plate layout");
                        }
                    });
        }
    }
    
    /**
     * Update well information by position
     * Can be used to set standard concentration, sample name, dilution factor, etc.
     */
    @PutMapping("/{plateLayoutId}/wells/{position}")
    @Operation(summary = "Update well information by position")
    public ResponseEntity<WellResponse> updateWellByPosition(
            @Parameter(description = "ID of the plate layout") @PathVariable Long plateLayoutId,
            @Parameter(description = "Position of the well (e.g., 'A1', 'B2')") @PathVariable String position,
            @Valid @RequestBody WellUpdateRequest request) {
        
        return plateLayoutRepository.findById(plateLayoutId)
                .flatMap(plateLayout -> {
                    // Find the well by position
                    int[] rowCol = WellPositionUtils.fromPosition(position);
                    return wellRepository.findByPlateLayoutIdAndRowAndColumn(
                            plateLayoutId, rowCol[0], rowCol[1]);
                })
                .map(well -> {
                    // Update well properties if they are provided in the request
                    if (request.getType() != null) {
                        well.setType(request.getType());
                    }
                    
                    // Update standard concentration if provided (for standard wells)
                    if (request.getStandardConcentration() != null) {
                        well.setStandardConcentration(request.getStandardConcentration());
                    }
                    
                    // Update sample information if provided (for sample wells)
                    if (request.getSampleName() != null) {
                        well.setSampleName(request.getSampleName());
                    }
                    
                    if (request.getDilutionFactor() != null) {
                        well.setDilutionFactor(request.getDilutionFactor());
                    }
                    
                    if (request.getReplicateGroup() != null) {
                        well.setReplicateGroup(request.getReplicateGroup());
                    }
                    
                    Well savedWell = wellRepository.save(well);
                    return ResponseEntity.ok(convertToWellResponse(savedWell));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Bulk update multiple wells at once
     */
    @PutMapping("/{plateLayoutId}/wells/bulk")
    @Operation(summary = "Bulk update multiple wells at once")
    public ResponseEntity<List<WellResponse>> updateWellsBulk(
            @Parameter(description = "ID of the plate layout") @PathVariable Long plateLayoutId,
            @Valid @RequestBody List<WellUpdateRequest> requests) {
        
        return plateLayoutRepository.findById(plateLayoutId)
                .map(plateLayout -> {
                    // Get all wells for this plate
                    List<Well> wells = wellRepository.findByPlateLayoutId(plateLayoutId);
                    
                    // Create a map of position to well for quick lookup
                    Map<String, Well> positionToWellMap = wells.stream()
                            .collect(Collectors.toMap(
                                    well -> WellPositionUtils.toPosition(well.getRow(), well.getColumn()),
                                    Function.identity()
                            ));
                    
                    // Process each update request
                    requests.forEach(request -> {
                        String position = request.getPosition().toUpperCase();
                        if (positionToWellMap.containsKey(position)) {
                            Well well = positionToWellMap.get(position);
                            
                            // Update well properties if they are provided in the request
                            if (request.getType() != null) {
                                well.setType(request.getType());
                            }
                            
                            if (request.getStandardConcentration() != null) {
                                well.setStandardConcentration(request.getStandardConcentration());
                            }
                            
                            if (request.getSampleName() != null) {
                                well.setSampleName(request.getSampleName());
                            }
                            
                            if (request.getDilutionFactor() != null) {
                                well.setDilutionFactor(request.getDilutionFactor());
                            }
                            
                            if (request.getReplicateGroup() != null) {
                                well.setReplicateGroup(request.getReplicateGroup());
                            }
                        }
                    });
                    
                    // Save all updated wells
                    List<Well> savedWells = wellRepository.saveAll(wells);
                    
                    // Convert to response
                    List<WellResponse> responses = savedWells.stream()
                            .map(this::convertToWellResponse)
                            .collect(Collectors.toList());
                            
                    return ResponseEntity.ok(responses);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private void updateWellTypes(Map<Long, Well> wellMap, List<Long> wellIds, WellType wellType) {
        if (wellIds != null) {
            wellIds.forEach(wellId -> {
                if (wellMap.containsKey(wellId)) {
                    wellMap.get(wellId).setType(wellType);
                }
            });
        }
    }

    private PlateLayoutResponse convertToResponse(PlateLayout plateLayout) {
        return PlateLayoutResponse.builder()
                .id(plateLayout.getId())
                .rows(plateLayout.getRows())
                .columns(plateLayout.getColumns())
                .projectId(plateLayout.getProject().getId())
                .wellCount(plateLayout.getWells() != null ? plateLayout.getWells().size() : 0)
                .build();
    }

    private WellResponse convertToWellResponse(Well well) {
        return WellResponse.builder()
                .id(well.getId())
                .row(well.getRow())
                .column(well.getColumn())
                .position(well.getPosition())
                .type(well.getType())
                .standardConcentration(well.getStandardConcentration())
                .sampleName(well.getSampleName())
                .dilutionFactor(well.getDilutionFactor())
                .replicateGroup(well.getReplicateGroup())
                .plateLayoutId(well.getPlateLayout().getId())
                .build();
    }
}