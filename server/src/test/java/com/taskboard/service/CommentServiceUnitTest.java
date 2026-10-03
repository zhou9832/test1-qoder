package com.taskboard.service;

import com.taskboard.common.BizException;
import com.taskboard.common.ErrorCode;
import com.taskboard.dto.CommentDto;
import com.taskboard.dto.CreateCommentRequest;
import com.taskboard.entity.Comment;
import com.taskboard.entity.Task;
import com.taskboard.repository.CommentRepository;
import com.taskboard.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CommentService business logic validation.
 * Uses Mockito to mock repository layer and test service behavior in isolation.
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceUnitTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private CommentService commentService;

    // ==================== Create Comment Tests ====================

    @Test
    @DisplayName("创建评论 - 正常路径应返回 CommentDto")
    void create_shouldSucceed_withValidRequest() {
        // Given
        Task mockTask = new Task(1L, "测试任务", "描述", "TODO", 0, null);
        mockTask.setId(1L);
        
        Comment mockComment = new Comment();
        mockComment.setId(1L);
        mockComment.setTask(mockTask);
        mockComment.setAuthor("张三");
        mockComment.setContent("这是一条评论内容");
        mockComment.setCreatedAt(Instant.now());

        CreateCommentRequest request = new CreateCommentRequest(1L, "张三", "这是一条评论内容");
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask));
        when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);

        // When
        CommentDto result = commentService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.taskId());
        assertEquals("张三", result.author());
        assertEquals("这是一条评论内容", result.content());
        assertNotNull(result.createdAt());
        
        verify(taskRepository).findById(1L);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("创建评论 - taskId 为 null 时应抛出 BizException")
    void create_shouldThrow_whenTaskIdIsNull() {
        // Given
        CreateCommentRequest request = new CreateCommentRequest(null, "张三", "评论内容");

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> commentService.create(request));
        assertEquals(ErrorCode.PARAM_REQUIRED.getCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains("任务ID不能为空"));
        
        verifyNoInteractions(taskRepository, commentRepository);
    }

    @Test
    @DisplayName("创建评论 - content 为 null 时应抛出 BizException")
    void create_shouldThrow_whenContentIsNull() {
        // Given
        CreateCommentRequest request = new CreateCommentRequest(1L, "张三", null);

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> commentService.create(request));
        assertEquals(ErrorCode.PARAM_REQUIRED.getCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains("评论内容不能为空"));
        
        verifyNoInteractions(taskRepository, commentRepository);
    }

    @Test
    @DisplayName("创建评论 - content 为空白字符串时应抛出 BizException")
    void create_shouldThrow_whenContentIsBlank() {
        // Given
        CreateCommentRequest request = new CreateCommentRequest(1L, "张三", "   ");

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> commentService.create(request));
        assertEquals(ErrorCode.PARAM_REQUIRED.getCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains("评论内容不能为空"));
        
        verifyNoInteractions(taskRepository, commentRepository);
    }

    @Test
    @DisplayName("创建评论 - author 超过 64 字符时应抛出 BizException")
    void create_shouldThrow_whenAuthorExceedsLength() {
        // Given
        String longAuthor = "a".repeat(65);
        CreateCommentRequest request = new CreateCommentRequest(1L, longAuthor, "评论内容");

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> commentService.create(request));
        assertEquals(ErrorCode.PARAM_LENGTH_EXCEEDED.getCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains("作者名不能超过64个字符"));
        
        verifyNoInteractions(taskRepository, commentRepository);
    }

    @Test
    @DisplayName("创建评论 - content 超过 1024 字符时应抛出 BizException")
    void create_shouldThrow_whenContentExceedsLength() {
        // Given - Note: actual code uses 1024 limit, not 2048 as mentioned in requirements
        String longContent = "b".repeat(1025);
        CreateCommentRequest request = new CreateCommentRequest(1L, "张三", longContent);

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> commentService.create(request));
        assertEquals(ErrorCode.PARAM_LENGTH_EXCEEDED.getCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains("评论内容不能超过1024个字符"));
        
        verifyNoInteractions(taskRepository, commentRepository);
    }

    @Test
    @DisplayName("创建评论 - 任务不存在时应抛出 BizException")
    void create_shouldThrow_whenTaskNotFound() {
        // Given
        CreateCommentRequest request = new CreateCommentRequest(999L, "张三", "评论内容");
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> commentService.create(request));
        assertEquals(ErrorCode.PARAM_NOT_FOUND.getCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains("任务不存在"));
        
        verify(taskRepository).findById(999L);
        verifyNoMoreInteractions(commentRepository);
    }

    // ==================== Find By Task ID Tests ====================

    @Test
    @DisplayName("分页查询评论 - 应返回分页数据")
    void findByTaskId_shouldReturnPaginatedComments() {
        // Given
        Task mockTask = new Task(1L, "测试任务", "描述", "TODO", 0, null);
        mockTask.setId(1L);

        Comment comment1 = new Comment();
        comment1.setId(1L);
        comment1.setTask(mockTask);
        comment1.setAuthor("张三");
        comment1.setContent("评论一");
        comment1.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));

        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setTask(mockTask);
        comment2.setAuthor("李四");
        comment2.setContent("评论二");
        comment2.setCreatedAt(Instant.parse("2026-01-01T09:00:00Z"));

        List<Comment> comments = List.of(comment1, comment2);
        Page<Comment> commentPage = new PageImpl<>(comments);

        when(commentRepository.findByTaskIdOrderByCreatedAtDesc(eq(1L), any(PageRequest.class)))
            .thenReturn(commentPage);

        // When
        Page<CommentDto> result = commentService.findAll(1L, 1, 20);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals("张三", result.getContent().get(0).author());
        assertEquals("评论一", result.getContent().get(0).content());
        assertEquals("李四", result.getContent().get(1).author());
        assertEquals("评论二", result.getContent().get(1).content());
        
        verify(commentRepository).findByTaskIdOrderByCreatedAtDesc(eq(1L), any(PageRequest.class));
    }

    @Test
    @DisplayName("分页查询评论 - 空列表应返回空的 Page")
    void findByTaskId_shouldReturnEmptyPage_whenNoComments() {
        // Given
        Page<Comment> emptyPage = new PageImpl<>(List.of());
        when(commentRepository.findByTaskIdOrderByCreatedAtDesc(eq(2L), any(PageRequest.class)))
            .thenReturn(emptyPage);

        // When
        Page<CommentDto> result = commentService.findAll(2L, 1, 20);

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        
        verify(commentRepository).findByTaskIdOrderByCreatedAtDesc(eq(2L), any(PageRequest.class));
    }

    // ==================== Delete Comment Tests ====================

    @Test
    @DisplayName("删除评论 - 评论存在时应成功删除")
    void delete_shouldSucceed_whenCommentExists() {
        // Given
        Long commentId = 1L;
        when(commentRepository.existsById(commentId)).thenReturn(true);
        doNothing().when(commentRepository).deleteById(commentId);

        // When
        commentService.delete(commentId);

        // Then
        verify(commentRepository).existsById(commentId);
        verify(commentRepository).deleteById(commentId);
    }

    @Test
    @DisplayName("删除评论 - 评论不存在时应抛出 BizException")
    void delete_shouldThrow_whenCommentNotFound() {
        // Given
        Long nonExistentId = 999L;
        when(commentRepository.existsById(nonExistentId)).thenReturn(false);

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> commentService.delete(nonExistentId));
        assertEquals(ErrorCode.PARAM_NOT_FOUND.getCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains("评论不存在"));
        
        verify(commentRepository).existsById(nonExistentId);
        verify(commentRepository, never()).deleteById(nonExistentId);
    }
}
