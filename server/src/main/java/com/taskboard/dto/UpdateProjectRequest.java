package com.taskboard.dto;

/**
 * Data Transfer Object for updating an existing project.
 */
public record UpdateProjectRequest(
    String name,
    String description
) {
}
