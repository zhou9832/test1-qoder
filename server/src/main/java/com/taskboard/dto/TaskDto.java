package com.taskboard.dto;

import java.util.List;

/**
 * Data Transfer Object for Task responses.
 */
public record TaskDto(
    Long id,
    Long projectId,
    String title,
    String description,
    String status,
    Integer priority,
    String dueAt,
    List<TagDto> tags,
    String createdAt,
    String updatedAt
) {
}
