package com.taskboard.dto;

/**
 * Data Transfer Object for creating a new project.
 */
public record CreateProjectRequest(
    String name,
    String description
) {
}
