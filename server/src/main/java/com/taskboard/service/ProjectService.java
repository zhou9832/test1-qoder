package com.taskboard.service;

import com.taskboard.entity.Project;
import com.taskboard.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Service for Project business logic.
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
    public List<Project> getAllProjects() {
        return projectRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get project by ID.
     */
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "Project not found with id: " + id
                ));
    }

    /**
     * Create a new project.
     */
    @Transactional
    public Project createProject(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Project name cannot be blank"
            );
        }

        if (projectRepository.existsByName(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Project name already exists: " + name
            );
        }

        Project project = new Project(name, description);
        return projectRepository.save(project);
    }

    /**
     * Update an existing project.
     */
    @Transactional
    public Project updateProject(Long id, String name, String description) {
        Project project = getProjectById(id);

        if (name != null && !name.isBlank()) {
            if (name.length() > 64) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Project name cannot exceed 64 characters"
                );
            }
            // Check uniqueness only if name changed or same name belongs to different project
            if (!project.getName().equals(name) && projectRepository.existsByName(name)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Project name already exists: " + name
                );
            }
            project.setName(name);
        }

        if (description != null) {
            if (description.length() > 512) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Project description cannot exceed 512 characters"
                );
            }
            project.setDescription(description);
        }

        return projectRepository.save(project);
    }

    /**
     * Delete a project by ID.
     * Cascade strategy: deleting project will cascade delete its tasks (defined in domain model).
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
}
