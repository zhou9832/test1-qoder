package com.taskboard.dto;

/**
 * Request DTO for creating a TimeLog entry.
 */
public record CreateTimeLogRequest(
    Long taskId,
    String hours,
    String workDate,
    String note
) {
}
