package com.taskboard.dto;

/**
 * Stats item for by-status aggregation.
 */
public record StatsByStatusItem(
    String status,
    long count,
    long totalHours
) {
}
