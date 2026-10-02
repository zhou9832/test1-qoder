package com.taskboard.dto;

/**
 * Stats item for by-week aggregation.
 */
public record StatsByWeekItem(
    String week,
    long totalHours,
    long taskCount
) {
}
