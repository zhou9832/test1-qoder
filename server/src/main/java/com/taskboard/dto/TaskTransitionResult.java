package com.taskboard.dto;

import java.time.format.DateTimeFormatter;

/**
 * Result of a task status transition.
 */
public record TaskTransitionResult(
    Long taskId,
    String previousStatus,
    String currentStatus,
    String transitionedAt
) {
    public static TaskTransitionResult of(Long taskId, String previousStatus, String currentStatus) {
        return new TaskTransitionResult(
            taskId,
            previousStatus,
            currentStatus,
            DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now())
        );
    }
}
