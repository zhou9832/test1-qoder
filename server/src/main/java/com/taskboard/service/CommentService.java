package com.taskboard.service;

import com.taskboard.common.BizException;
import com.taskboard.common.ErrorCode;
import com.taskboard.dto.CommentDto;
import com.taskboard.dto.CreateCommentRequest;
import com.taskboard.entity.Comment;
import com.taskboard.entity.Task;
import com.taskboard.repository.CommentRepository;
import com.taskboard.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for Comment management.
 */
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;

    public CommentService(CommentRepository commentRepository, TaskRepository taskRepository) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
    }

    /**
     * Get comments for a task with pagination.
     */
    @Transactional(readOnly = true)
    public Page<CommentDto> findAll(Long taskId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<Comment> comments = commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId, pageRequest);
        return comments.map(CommentDto::from);
    }

    /**
     * Create a new comment.
     */
    @Transactional
    public CommentDto create(CreateCommentRequest request) {
        // Validate required fields
        if (request.taskId() == null) {
            throw new BizException(ErrorCode.PARAM_REQUIRED, "任务ID不能为空");
        }
        if (request.content() == null || request.content().isBlank()) {
            throw new BizException(ErrorCode.PARAM_REQUIRED, "评论内容不能为空");
        }

        // Validate content length
        if (request.content().length() > 1024) {
            throw new BizException(ErrorCode.PARAM_LENGTH_EXCEEDED, "评论内容不能超过1024个字符");
        }

        // Validate author name length
        if (request.author() != null && request.author().length() > 64) {
            throw new BizException(ErrorCode.PARAM_LENGTH_EXCEEDED, "作者名不能超过64个字符");
        }

        // Validate task exists
        Task task = taskRepository.findById(request.taskId())
            .orElseThrow(() -> new BizException(ErrorCode.PARAM_NOT_FOUND, "任务不存在"));

        Comment comment = new Comment();
        comment.setTask(task);
        comment.setAuthor(request.author() != null ? request.author().trim() : null);
        comment.setContent(request.content().trim());

        Comment saved = commentRepository.save(comment);
        return CommentDto.from(saved);
    }

    /**
     * Delete a comment.
     */
    @Transactional
    public void delete(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new BizException(ErrorCode.PARAM_NOT_FOUND, "评论不存在");
        }
        commentRepository.deleteById(id);
    }
}
