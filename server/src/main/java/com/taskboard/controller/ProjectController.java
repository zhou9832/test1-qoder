package com.taskboard.controller;

import com.taskboard.dto.ApiResponse;
import com.taskboard.entity.Project;
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
    public ApiResponse<List<Project>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        return ApiResponse.success(projects);
    }

    /**
     * Create a new project.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Project> createProject(
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "") String description
    ) {
        Project project = projectService.createProject(name, description);
        return ApiResponse.success(project);
    }

    /**
     * Get a project by ID.
     */
    @GetMapping("/{id}")
    public ApiResponse<Project> getProject(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        return ApiResponse.success(project);
    }

    /**
     * Update an existing project.
     */
    @PutMapping("/{id}")
    public ApiResponse<Project> updateProject(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description
    ) {
        Project project = projectService.updateProject(id, name, description);
        return ApiResponse.success(project);
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
