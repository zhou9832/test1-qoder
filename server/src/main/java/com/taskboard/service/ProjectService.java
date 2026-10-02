package com.taskboard.service;

import com.taskboard.dto.CreateProjectRequest;
import com.taskboard.dto.ProjectDto;
import com.taskboard.dto.UpdateProjectRequest;
import com.taskboard.entity.Project;
import com.taskboard.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for Project management.
 */
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * Get all projects ordered by creation time descending.
     */
    @Transactional(readOnly = true)
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toDto)
            .toList();
    }

    /**
     * Get a project by ID.
     */
    @Transactional(readOnly = true)
    public Optional<ProjectDto> getProjectById(Long id) {
        return projectRepository.findById(id).map(this::toDto);
    }

    /**
     * Create a new project with input validation.
     */
    @Transactional
    public ProjectDto createProject(CreateProjectRequest request) {
        validateProjectName(request.name());
        
        if (projectRepository.existsByName(request.name())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, 
                "Project name already exists: " + request.name()
            );
        }

        Project project = new Project();
        project.setName(request.name().trim());
        project.setDescription(request.description() != null ? request.description().trim() : "");
        
        validateDescriptionLength(project.getDescription());

        Project saved = projectRepository.save(project);
        return toDto(saved);
    }

    /**
     * Update an existing project with validation.
     */
    @Transactional
    public ProjectDto updateProject(Long id, UpdateProjectRequest request) {
        Project existing = projectRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "Project not found with id: " + id
            ));

        // Validate name if provided
        if (request.name() != null && !request.name().isEmpty()) {
            validateProjectName(request.name());
            
            // Check name uniqueness excluding current project
            if (projectRepository.existsByName(request.name()) 
                && !request.name().equals(existing.getName())) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT, 
                    "Project name already exists: " + request.name()
                );
            }
            
            existing.setName(request.name().trim());
        }

        // Update description if provided
        if (request.description() != null) {
            String desc = request.description().trim();
            validateDescriptionLength(desc);
            existing.setDescription(desc);
        }

        Project saved = projectRepository.save(existing);
        return toDto(saved);
    }

    /**
     * Delete a project by ID.
     * Cascade strategy: deleting project cascades to its tasks (defined in PRD).
     */
    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "Project not found with id: " + id
            );
        }
        projectRepository.deleteById(id);
    }

    /**
     * Check if a project exists by ID.
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return projectRepository.existsById(id);
    }

    // Private validation methods

    private void validateProjectName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Project name cannot be blank"
            );
        }
        if (name.length() > 64) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Project name must be 64 characters or less"
            );
        }
    }

    private void validateDescriptionLength(String description) {
        if (description.length() > 512) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Project description must be 512 characters or less"
            );
        }
    }

    /**
     * Convert Entity to DTO with ISO-8601 date formatting.
     */
    private ProjectDto toDto(Project project) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        return new ProjectDto(
            project.getId(),
            project.getName(),
            project.getDescription(),
            formatter.format(project.getCreatedAt()),
            formatter.format(project.getUpdatedAt())
        );
    }
}
