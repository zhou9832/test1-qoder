package com.taskboard.dto;

/**
 * Data Transfer Object for Project responses.
 */
public record ProjectDto(
    Long id,
    String name,
    String description,
    String createdAt,
    String updatedAt
) {
}
