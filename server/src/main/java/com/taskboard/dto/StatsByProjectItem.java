package com.taskboard.dto;

/**
 * Stats item for by-project aggregation.
 */
public record StatsByProjectItem(
    Long projectId,
    String projectName,
    long taskCount,
    long totalHours
) {
}
