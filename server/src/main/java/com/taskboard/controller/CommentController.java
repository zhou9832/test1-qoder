package com.taskboard.controller;

import com.taskboard.common.PageResult;
import com.taskboard.dto.ApiResponse;
import com.taskboard.dto.CommentDto;
import com.taskboard.dto.CreateCommentRequest;
import com.taskboard.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Comment management.
 */
@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Get comments for a task with pagination.
     */
    @GetMapping("/tasks/{taskId}/comments")
    public ApiResponse<PageResult<CommentDto>> getComments(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (size > 100) {
            size = 100;
        }
        Page<CommentDto> result = commentService.findAll(taskId, page, size);
        PageResult<CommentDto> pageResult = PageResult.of(
            result.getContent(),
            result.getTotalElements(),
            page,
            size
        );
        return ApiResponse.success(pageResult);
    }

    /**
     * Create a new comment.
     */
    @PostMapping("/tasks/{taskId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentDto> createComment(
            @PathVariable Long taskId,
            CreateCommentRequest request
    ) {
        // Override taskId from path variable (ensures consistency)
        request = new CreateCommentRequest(taskId, request.author(), request.content());
        CommentDto created = commentService.create(request);
        return ApiResponse.success(created);
    }

    /**
     * Delete a comment.
     */
    @DeleteMapping("/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long id) {
        commentService.delete(id);
    }
}
