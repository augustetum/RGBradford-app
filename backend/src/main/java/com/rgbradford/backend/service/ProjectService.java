package com.rgbradford.backend.service;

import com.rgbradford.backend.dto.request.ProjectRequest;
import com.rgbradford.backend.dto.response.ProjectResponse;
import com.rgbradford.backend.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectService {
    ProjectResponse createProject(ProjectRequest request, Long userId);
    ProjectResponse getProjectById(Long id);
    Page<ProjectResponse> getAllProjects(Pageable pageable, Long userId);
    ProjectResponse updateProject(Long id, ProjectRequest request);
    void deleteProject(Long id);
    Project getProjectEntity(Long id);
}
