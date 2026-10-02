package com.taskboard.dto;

/**
 * Data Transfer Object for updating an existing task.
 */
public record UpdateTaskRequest(
    String title,
    String description,
    Integer priority,
    String dueAt,
    java.util.List<Long> tagIds
) {
}
