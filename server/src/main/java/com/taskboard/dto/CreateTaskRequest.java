package com.taskboard.dto;

/**
 * Data Transfer Object for creating a new task.
 */
public record CreateTaskRequest(
    Long projectId,
    String title,
    String description,
    String status,
    Integer priority,
    String dueAt,
    java.util.List<Long> tagIds
) {
}
