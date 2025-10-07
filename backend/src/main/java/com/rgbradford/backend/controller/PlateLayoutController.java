package com.rgbradford.backend.controller;

import com.rgbradford.backend.dto.request.CreatePlateLayoutRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.rgbradford.backend.dto.request.UpdatePlateLayoutRequest;
import com.rgbradford.backend.dto.request.WellRequest;
import com.rgbradford.backend.dto.request.WellUpdateRequest;
import com.rgbradford.backend.dto.response.PlateLayoutResponse;
import com.rgbradford.backend.dto.response.WellResponse;
import com.rgbradford.backend.entity.PlateLayout;
import com.rgbradford.backend.entity.Project;
import com.rgbradford.backend.entity.Well;
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Controller for managing plate layouts and their associated wells.
 * Provides CRUD operations for plate layouts and their well configurations.
 */
@RestController
@RequestMapping("/api/plate-layouts")
@Tag(
    name = "Plate Layout",
    description = "APIs for managing plate layouts and their well configurations. " +
                 "Plate layouts define the arrangement of samples, standards, and controls in microplates."
)
@SecurityRequirement(name = "bearerAuth")
public class PlateLayoutController {

    @Autowired
    private PlateLayoutRepository plateLayoutRepository;

    @Autowired
    private WellRepository wellRepository;

    @Autowired
    private ProjectRepository projectRepository;

    // ========== POST METHODS ==========

    /**
     * Create a new plate layout with the specified configuration.
     *
     * @param request The plate layout creation request
     * @param userDetails The authenticated user details
     * @return The created plate layout with its initial configuration
     */
    @Operation(
        summary = "Create a new plate layout",
        description = "Creates a new plate layout with the specified configuration. " +
                    "The authenticated user must have access to the specified project."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Successfully created the plate layout"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request or project already has a plate layout"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Not authorized to access the specified project"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Project not found"
        )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ResponseEntity<PlateLayoutResponse> createPlateLayout(
            @Valid @RequestBody CreatePlateLayoutRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

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

    @Operation(
        summary = "Add wells to plate layout",
        description = "Batch creates multiple wells for a plate layout. " +
                "This endpoint is optimized for performance using batch operations. " +
                "Use this when setting up a new plate layout with many wells."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Wells successfully created",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Batch well creation response",
                    value = """
                        [
                          {
                            "id": 1,
                            "row": 0,
                            "column": 0,
                            "position": "A1",
                            "type": "STANDARD",
                            "standardConcentration": 0.5,
                            "plateLayoutId": 1
                          },
                          {
                            "id": 2,
                            "row": 0,
                            "column": 1,
                            "position": "A2",
                            "type": "SAMPLE",
                            "sampleName": "Sample 1",
                            "dilutionFactor": 10.0,
                            "replicateGroup": "G1",
                            "plateLayoutId": 1
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Plate layout not found"
        )
    })
    @PostMapping("/{id}/wells")
    @Transactional
    public ResponseEntity<List<WellResponse>> addWellsToPlate(
            @Parameter(description = "ID of the plate layout", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "List of wells to create",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "Batch well creation",
                        value = """
                            [
                              {
                                "row": 0,
                                "column": 0,
                                "type": "STANDARD",
                                "standardConcentration": 0.5,
                                "dilutionFactor": 1.0,
                                "replicateGroup": "STD1"
                              },
                              {
                                "row": 0,
                                "column": 1,
                                "type": "SAMPLE",
                                "sampleName": "Sample 1",
                                "dilutionFactor": 10.0,
                                "replicateGroup": "G1"
                              }
                            ]
                            """
                    )
                )
            )
            @RequestBody List<WellRequest> wellRequests) {

        if (!plateLayoutRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // Build all wells in parallel
        List<Well> wells = wellRequests.parallelStream()
                .map(request -> buildWell(request, id))
                .collect(Collectors.toList());

        // Single batch save
        List<Well> savedWells = wellRepository.saveAll(wells);

        // Convert to responses in parallel
        List<WellResponse> responses = savedWells.parallelStream()
                .map(this::convertToWellResponse)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    // ========== GET METHODS ==========

    /**
     * Retrieve all plate layouts with pagination support.
     *
     * @param pageable Pagination information (page, size, sort)
     * @return Page of plate layouts with their basic information
     */
    @Operation(
        summary = "List all plate layouts",
        description = "Retrieves a paginated list of all plate layouts in the system. " +
                    "Results can be sorted and filtered using standard Spring Data parameters."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved list of plate layouts"
    )
    @GetMapping
    public ResponseEntity<Page<PlateLayoutResponse>> getAllPlateLayouts(
            @Parameter(hidden = true) Pageable pageable) {
        Page<PlateLayout> plateLayouts = plateLayoutRepository.findAll(pageable);
        Page<PlateLayoutResponse> responses = plateLayouts.map(this::convertToResponse);
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieve a specific plate layout by its ID.
     *
     * @param id The ID of the plate layout to retrieve
     * @return The requested plate layout with all its wells and configurations
     */
    @Operation(
        summary = "Get plate layout by ID",
        description = "Retrieves a specific plate layout with all its well configurations."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved the plate layout"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Plate layout not found"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<PlateLayoutResponse> getPlateLayout(
            @Parameter(description = "ID of the plate layout to retrieve", required = true)
            @PathVariable Long id) {
        return plateLayoutRepository.findById(id)
                .map(this::convertToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Get plate layout ID by project",
        description = "Retrieves the plate layout ID associated with a specific project. " +
                "Since each project can have only one plate layout, this returns a simple ID mapping."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved plate layout ID",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "plateLayoutId": 1
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Project not found or project does not have a plate layout"
        )
    })
    @GetMapping("/by-project/{projectId}")
    public ResponseEntity<Map<String, Long>> getPlateIdByProject(
            @Parameter(description = "ID of the project", required = true, example = "1")
            @PathVariable Long projectId) {
        return projectRepository.findById(projectId)
                .map(Project::getPlateLayout)
                .filter(pl -> pl != null)
                .map(pl -> ResponseEntity.ok(Map.of("plateLayoutId", pl.getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Get all wells for a plate layout",
        description = "Retrieves all wells associated with a specific plate layout (non-paginated). " +
                "Useful for getting the complete plate configuration."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved wells"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Plate layout not found"
        )
    })
    @GetMapping("/{id}/wells")
    public ResponseEntity<List<WellResponse>> getWellsForPlate(
            @Parameter(description = "ID of the plate layout", required = true, example = "1")
            @PathVariable Long id) {
        if (!plateLayoutRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        List<Well> wells = wellRepository.findByPlateLayoutId(id);
        List<WellResponse> responses = wells.stream()
                .map(this::convertToWellResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    // ========== PUT METHODS ==========

    @Operation(
        summary = "Update plate layout",
        description = "Updates an existing plate layout's dimensions. " +
                "Warning: Changing dimensions may affect existing wells and analysis results."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Plate layout successfully updated"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Plate layout not found"
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<PlateLayoutResponse> updatePlateLayout(
            @Parameter(description = "ID of the plate layout to update", required = true, example = "1")
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

    @Operation(
        summary = "Update multiple wells in a plate layout",
        description = "Batch updates or creates wells using their position notation (e.g., 'A1', 'B2'). " +
                "Only the fields provided in the request will be updated. " +
                "If a well doesn't exist at the specified position, it will be created."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Wells successfully updated",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        [
                          {
                            "id": 1,
                            "row": 0,
                            "column": 0,
                            "position": "A1",
                            "type": "STANDARD",
                            "standardConcentration": 0.75,
                            "plateLayoutId": 1
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid position format"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Plate layout not found"
        )
    })
    @PutMapping("/{id}/wells")
    @Transactional
    public ResponseEntity<List<WellResponse>> updateWellsInPlate(
            @Parameter(description = "ID of the plate layout", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "List of well updates using position notation",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        value = """
                            [
                              {
                                "position": "A1",
                                "type": "STANDARD",
                                "standardConcentration": 0.75
                              },
                              {
                                "position": "B1",
                                "sampleName": "Updated Sample",
                                "dilutionFactor": 5.0
                              }
                            ]
                            """
                    )
                )
            )
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

    @Operation(
        summary = "Update well information by position",
        description = "Updates a single well using its position notation (e.g., 'A1', 'B2'). " +
                "Only the fields provided in the request will be updated."
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
            description = "Plate layout or well not found"
        )
    })
    @PutMapping("/{plateLayoutId}/wells/{position}")
    public ResponseEntity<WellResponse> updateWellByPosition(
            @Parameter(description = "ID of the plate layout", required = true, example = "1")
            @PathVariable Long plateLayoutId,
            @Parameter(description = "Position of the well (e.g., 'A1', 'B2')", required = true, example = "A1")
            @PathVariable String position,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Well update request",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        value = """
                            {
                              "type": "SAMPLE",
                              "sampleName": "Updated Sample Name",
                              "dilutionFactor": 5.0,
                              "replicateGroup": "G2"
                            }
                            """
                    )
                )
            )
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

    // ========== DELETE METHODS ==========

    @Operation(
        summary = "Delete plate layout",
        description = "Permanently deletes a plate layout and all associated wells and analysis results. " +
                "This action cannot be undone."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Plate layout successfully deleted"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Plate layout not found"
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlateLayout(
            @Parameter(description = "ID of the plate layout to delete", required = true, example = "1")
            @PathVariable Long id) {
        if (!plateLayoutRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        plateLayoutRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ========== PRIVATE HELPER METHODS ==========

    private Well buildWell(WellRequest request, Long plateLayoutId) {
        String position = WellPositionUtils.toPosition(request.getRow(), request.getColumn());

        // Use getReferenceById - creates a proxy without database hit
        PlateLayout plateLayoutProxy = plateLayoutRepository.getReferenceById(plateLayoutId);

        return Well.builder()
                .row(request.getRow())
                .column(request.getColumn())
                .position(position)
                .type(request.getType())
                .standardConcentration(request.getStandardConcentration())
                .sampleName(request.getSampleName())
                .dilutionFactor(request.getDilutionFactor())
                .replicateGroup(request.getReplicateGroup())
                .plateLayout(plateLayoutProxy) // Use the proxy
                .build();
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
