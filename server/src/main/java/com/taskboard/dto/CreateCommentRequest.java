package com.taskboard.dto;

/**
 * Request DTO for creating a Comment.
 */
public record CreateCommentRequest(
    Long taskId,
    String author,
    String content
) {
}
