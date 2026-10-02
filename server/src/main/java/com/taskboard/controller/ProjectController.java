package com.taskboard.controller;

import com.taskboard.dto.ApiResponse;
import com.taskboard.dto.CreateProjectRequest;
import com.taskboard.dto.ProjectDto;
import com.taskboard.dto.UpdateProjectRequest;
import com.taskboard.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Project management.
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * Get all projects.
     */
    @GetMapping
    public ApiResponse<List<ProjectDto>> getAllProjects() {
        List<ProjectDto> projects = projectService.getAllProjects();
        return ApiResponse.success(projects);
    }

    /**
     * Create a new project.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ProjectDto> createProject(
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "") String description
    ) {
        CreateProjectRequest request = new CreateProjectRequest(name, description);
        ProjectDto created = projectService.createProject(request);
        return ApiResponse.success(created);
    }

    /**
     * Get a project by ID.
     */
    @GetMapping("/{id}")
    public ApiResponse<ProjectDto> getProjectById(@PathVariable Long id) {
        ProjectDto project = projectService.getProjectById(id)
            .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + id));
        return ApiResponse.success(project);
    }

    /**
     * Update an existing project.
     */
    @PutMapping("/{id}")
    public ApiResponse<ProjectDto> updateProject(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description
    ) {
        UpdateProjectRequest request = new UpdateProjectRequest(name, description);
        ProjectDto updated = projectService.updateProject(id, request);
        return ApiResponse.success(updated);
    }

    /**
     * Delete a project by ID.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ApiResponse.success(null);
    }
}
