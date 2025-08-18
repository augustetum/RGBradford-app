package com.rgbradford.backend.controller;

import com.rgbradford.backend.dto.request.CreatePlateLayoutRequest;
import com.rgbradford.backend.dto.request.UpdatePlateLayoutRequest;
import com.rgbradford.backend.dto.request.WellRequest;
import com.rgbradford.backend.dto.response.PlateLayoutResponse;
import com.rgbradford.backend.dto.response.WellResponse;
import com.rgbradford.backend.entity.PlateLayout;
import com.rgbradford.backend.entity.Well;
import com.rgbradford.backend.entity.WellType;
import com.rgbradford.backend.repository.PlateLayoutRepository;
import com.rgbradford.backend.repository.WellRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/plate-layouts")
public class PlateLayoutController {

    @Autowired
    private PlateLayoutRepository plateLayoutRepository;

    @Autowired
    private WellRepository wellRepository;

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
    public ResponseEntity<PlateLayoutResponse> createPlateLayout(@RequestBody CreatePlateLayoutRequest request) {
        PlateLayout plateLayout = PlateLayout.builder()
                .rows(request.getRows())
                .columns(request.getColumns())
                .project(request.getProject())
                .build();
        
        PlateLayout saved = plateLayoutRepository.save(plateLayout);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponse(saved));
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
                            .map(request -> Well.builder()
                                    .row(request.getRow())
                                    .column(request.getColumn())
                                    .type(request.getType())
                                    .standardConcentration(request.getStandardConcentration())
                                    .sampleName(request.getSampleName())
                                    .dilutionFactor(request.getDilutionFactor())
                                    .replicateGroup(request.getReplicateGroup())
                                    .plateLayout(plateLayout)
                                    .build())
                            .collect(Collectors.toList());
                    
                    List<Well> savedWells = wellRepository.saveAll(wells);
                    List<WellResponse> responses = savedWells.stream()
                            .map(this::convertToWellResponse)
                            .collect(Collectors.toList());
                    
                    return ResponseEntity.status(HttpStatus.CREATED).body(responses);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/plate-layouts/{id}/wells - Update wells in plate layout
    @PutMapping("/{id}/wells")
    public ResponseEntity<List<WellResponse>> updateWellsInPlate(
            @PathVariable Long id, 
            @RequestBody List<WellRequest> wellRequests) {
        
        return plateLayoutRepository.findById(id)
                .map(plateLayout -> {
                    // Delete existing wells
                    wellRepository.deleteByPlateLayoutId(id);
                    
                    // Create new wells
                    List<Well> wells = wellRequests.stream()
                            .map(request -> Well.builder()
                                    .row(request.getRow())
                                    .column(request.getColumn())
                                    .type(request.getType())
                                    .standardConcentration(request.getStandardConcentration())
                                    .sampleName(request.getSampleName())
                                    .dilutionFactor(request.getDilutionFactor())
                                    .replicateGroup(request.getReplicateGroup())
                                    .plateLayout(plateLayout)
                                    .build())
                            .collect(Collectors.toList());
                    
                    List<Well> savedWells = wellRepository.saveAll(wells);
                    List<WellResponse> responses = savedWells.stream()
                            .map(this::convertToWellResponse)
                            .collect(Collectors.toList());
                    
                    return ResponseEntity.ok(responses);
                })
                .orElse(ResponseEntity.notFound().build());
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
                .type(well.getType())
                .standardConcentration(well.getStandardConcentration())
                .sampleName(well.getSampleName())
                .dilutionFactor(well.getDilutionFactor())
                .replicateGroup(well.getReplicateGroup())
                .plateLayoutId(well.getPlateLayout().getId())
                .build();
    }
} 