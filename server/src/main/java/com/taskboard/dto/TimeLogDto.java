package com.taskboard.dto;

import java.time.format.DateTimeFormatter;
import com.taskboard.entity.TimeLog;

/**
 * DTO for TimeLog responses.
 */
public record TimeLogDto(
    Long id,
    Long taskId,
    String hours,
    String workDate,
    String note,
    String createdAt
) {
    public static TimeLogDto from(TimeLog log) {
        return new TimeLogDto(
            log.getId(),
            log.getTaskId(),
            log.getHours(),
            log.getWorkDate() != null ? log.getWorkDate().toString() : null,
            log.getNote(),
            log.getCreatedAt() != null ? DateTimeFormatter.ISO_INSTANT.format(log.getCreatedAt()) : null
        );
    }
}
