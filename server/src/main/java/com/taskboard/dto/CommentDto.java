package com.taskboard.dto;

import java.time.format.DateTimeFormatter;
import com.taskboard.entity.Comment;

/**
 * DTO for Comment responses.
 */
public record CommentDto(
    Long id,
    Long taskId,
    String author,
    String content,
    String createdAt
) {
    public static CommentDto from(Comment comment) {
        return new CommentDto(
            comment.getId(),
            comment.getTaskId(),
            comment.getAuthor(),
            comment.getContent(),
            comment.getCreatedAt() != null ? DateTimeFormatter.ISO_INSTANT.format(comment.getCreatedAt()) : null
        );
    }
}
