package com.rgbradford.backend.controller;

import com.rgbradford.backend.dto.request.ProjectRequest;
import com.rgbradford.backend.dto.response.ProjectResponse;
import com.rgbradford.backend.entity.User;
import com.rgbradford.backend.repository.UserRepository;
import com.rgbradford.backend.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Validated
@Tag(
    name = "Project",
    description = "APIs for managing research projects. Projects are containers for plate layouts and analysis workflows. " +
                 "Each project can have one plate layout and multiple analysis sessions."
)
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;
    private final UserRepository userRepository;

    @Operation(
        summary = "Create a new project",
        description = "Creates a new research project for the authenticated user. " +
                "Each project can contain one plate layout and serves as a container for organizing analysis workflows."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Project successfully created",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProjectResponse.class),
                examples = @ExampleObject(
                    name = "Created project",
                    value = """
                        {
                          "id": 1,
                          "name": "Bradford Assay - Cell Lysates",
                          "description": "Protein quantitation of HeLa cell lysates",
                          "createdAt": "2025-01-15T10:30:00",
                          "updatedAt": "2025-01-15T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - validation failed"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Project creation details",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = ProjectRequest.class),
                    examples = @ExampleObject(
                        name = "Project creation request",
                        value = """
                            {
                              "name": "Bradford Assay - Cell Lysates",
                              "description": "Protein quantitation of HeLa cell lysates"
                            }
                            """
                    )
                )
            )
            @Valid @RequestBody ProjectRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // Get the currently authenticated user's email from the security context
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        ProjectResponse response = projectService.createProject(request, user.getId());
        return ResponseEntity
                .created(URI.create("/api/projects/" + response.getId()))
                .body(response);
    }

    @Operation(
        summary = "Get project by ID",
        description = "Retrieves detailed information about a specific project including its metadata and timestamps."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved project",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProjectResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "id": 1,
                          "name": "Bradford Assay - Cell Lysates",
                          "description": "Protein quantitation of HeLa cell lysates",
                          "createdAt": "2025-01-15T10:30:00",
                          "updatedAt": "2025-01-15T14:22:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Project not found"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(
            @Parameter(
                description = "ID of the project to retrieve",
                required = true,
                example = "1"
            )
            @PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @Operation(
        summary = "List all projects",
        description = "Retrieves a paginated list of all projects belonging to the authenticated user. " +
                "Results can be sorted and paginated using standard Spring Data parameters."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved projects",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Paginated projects",
                    value = """
                        {
                          "content": [
                            {
                              "id": 1,
                              "name": "Bradford Assay - Cell Lysates",
                              "description": "Protein quantitation of HeLa cell lysates",
                              "createdAt": "2025-01-15T10:30:00",
                              "updatedAt": "2025-01-15T14:22:00"
                            },
                            {
                              "id": 2,
                              "name": "BSA Standard Curve",
                              "description": "BSA protein standards for calibration",
                              "createdAt": "2025-01-14T09:15:00",
                              "updatedAt": "2025-01-14T09:15:00"
                            }
                          ],
                          "pageable": {
                            "pageNumber": 0,
                            "pageSize": 20
                          },
                          "totalElements": 2,
                          "totalPages": 1
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @GetMapping
    public ResponseEntity<Page<ProjectResponse>> getAllProjects(
            @Parameter(hidden = true) Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        Page<ProjectResponse> page = projectService.getAllProjects(pageable, user.getId());
        return ResponseEntity.ok(page);
    }

    @Operation(
        summary = "Update project",
        description = "Updates an existing project's name and/or description. " +
                "All fields in the request will replace the existing values."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Project successfully updated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProjectResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "id": 1,
                          "name": "Bradford Assay - Updated Name",
                          "description": "Updated description for protein quantitation",
                          "createdAt": "2025-01-15T10:30:00",
                          "updatedAt": "2025-01-15T16:45:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - validation failed"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Project not found"
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @Parameter(
                description = "ID of the project to update",
                required = true,
                example = "1"
            )
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Updated project details",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = ProjectRequest.class),
                    examples = @ExampleObject(
                        value = """
                            {
                              "name": "Bradford Assay - Updated Name",
                              "description": "Updated description for protein quantitation"
                            }
                            """
                    )
                )
            )
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @Operation(
        summary = "Delete project",
        description = "Permanently deletes a project and all associated data including plate layouts, wells, and analysis results. " +
                "This action cannot be undone."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Project successfully deleted"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Project not found"
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @Parameter(
                description = "ID of the project to delete",
                required = true,
                example = "1"
            )
            @PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
