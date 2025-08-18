package com.rgbradford.backend.controller;

import com.rgbradford.backend.dto.request.WellRequest;
import com.rgbradford.backend.dto.response.WellResponse;
import com.rgbradford.backend.entity.Well;
import com.rgbradford.backend.entity.WellType;
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
@RequestMapping("/api/wells")
public class WellController {

    @Autowired
    private WellRepository wellRepository;

    // GET /api/wells - List all wells (with filters)
    @GetMapping
    public ResponseEntity<Page<WellResponse>> getAllWells(
            @RequestParam(required = false) Long plateLayoutId,
            @RequestParam(required = false) WellType wellType,
            @RequestParam(required = false) String replicateGroup,
            Pageable pageable) {
        
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

    // GET /api/wells/{id} - Get specific well
    @GetMapping("/{id}")
    public ResponseEntity<WellResponse> getWell(@PathVariable Long id) {
        return wellRepository.findById(id)
                .map(this::convertToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/wells - Create new well
    @PostMapping
    public ResponseEntity<WellResponse> createWell(@RequestBody WellRequest request) {
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

    // PUT /api/wells/{id} - Update well
    @PutMapping("/{id}")
    public ResponseEntity<WellResponse> updateWell(
            @PathVariable Long id, 
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

    // DELETE /api/wells/{id} - Delete well
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWell(@PathVariable Long id) {
        if (!wellRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        wellRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/wells/plate/{plateLayoutId} - Get wells by plate
    @GetMapping("/plate/{plateLayoutId}")
    public ResponseEntity<List<WellResponse>> getWellsByPlate(@PathVariable Long plateLayoutId) {
        List<Well> wells = wellRepository.findByPlateLayoutId(plateLayoutId);
        List<WellResponse> responses = wells.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    // GET /api/wells/type/{wellType} - Get wells by type
    @GetMapping("/type/{wellType}")
    public ResponseEntity<Page<WellResponse>> getWellsByType(
            @PathVariable WellType wellType, 
            Pageable pageable) {
        
        Page<Well> wells = wellRepository.findByType(wellType, pageable);
        Page<WellResponse> responses = wells.map(this::convertToResponse);
        
        return ResponseEntity.ok(responses);
    }

    // GET /api/wells/replicate/{group} - Get wells by replicate group
    @GetMapping("/replicate/{group}")
    public ResponseEntity<Page<WellResponse>> getWellsByReplicateGroup(
            @PathVariable String group, 
            Pageable pageable) {
        
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