package com.taskboard.dto;

/**
 * Data Transfer Object for Tag responses.
 */
public record TagDto(
    Long id,
    String name,
    String createdAt,
    String updatedAt
) {
}
