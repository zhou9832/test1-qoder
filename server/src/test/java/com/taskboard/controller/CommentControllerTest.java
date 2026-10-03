package com.taskboard.controller;

import com.taskboard.common.BizException;
import com.taskboard.common.ErrorCode;
import com.taskboard.dto.CommentDto;
import com.taskboard.dto.CreateCommentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.taskboard.service.CommentService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.taskboard.service.CommentService;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CommentController using MockMvc.
 * Tests HTTP semantics: status codes, ApiResponse wrapper, and request/response formatting.
 */
public class CommentControllerTest {

    private MockMvc mockMvc;

    private CommentService commentService = org.mockito.Mockito.mock(CommentService.class);

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new CommentController(commentService))
            .build();
    }

    // ==================== Get Comments Tests ====================

    @Test
    @DisplayName("获取评论列表 - 应返回分页数据")
    void getComments_shouldReturnPageOfComments() throws Exception {
        // Given
        CommentDto comment1 = new CommentDto(1L, 1L, "张三", "评论一", 
            Instant.parse("2026-01-01T10:00:00Z").toString());
        CommentDto comment2 = new CommentDto(2L, 1L, "李四", "评论二",
            Instant.parse("2026-01-01T09:00:00Z").toString());

        Page<CommentDto> page = new PageImpl<>(List.of(comment1, comment2));
        given(commentService.findAll(eq(1L), eq(1), eq(20))).willReturn(page);

        // When & Then
        mockMvc.perform(get("/api/tasks/1/comments")
                .param("page", "1")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.items", hasSize(2)))
            .andExpect(jsonPath("$.data.items[0].id").value(1))
            .andExpect(jsonPath("$.data.items[0].author").value("张三"))
            .andExpect(jsonPath("$.data.items[0].content").value("评论一"))
            .andExpect(jsonPath("$.data.total").value(2))
            .andExpect(jsonPath("$.data.page").value(1))
            .andExpect(jsonPath("$.data.size").value(20));
    }

    @Test
    @DisplayName("获取评论列表 - 空列表时应返回空的 items")
    void getComments_shouldReturnEmptyItems_whenNoComments() throws Exception {
        // Given
        Page<CommentDto> emptyPage = new PageImpl<>(List.of());
        given(commentService.findAll(eq(999L), eq(1), eq(20))).willReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/api/tasks/999/comments")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.items", hasSize(0)))
            .andExpect(jsonPath("$.data.total").value(0));
    }

    // ==================== Create Comment Tests ====================

    @Test
    @DisplayName("创建评论 - 应返回 201 CREATED")
    void createComment_shouldReturnCreated_WithStatus201() throws Exception {
        // Given
        CommentDto createdComment = new CommentDto(1L, 1L, "张三", "新评论内容",
            Instant.now().toString());
        
        given(commentService.create(any(CreateCommentRequest.class))).willReturn(createdComment);

        // When & Then
        mockMvc.perform(post("/api/tasks/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"taskId\":1,\"author\":\"张三\",\"content\":\"新评论内容\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.taskId").value(1))
            .andExpect(jsonPath("$.data.author").value("张三"))
            .andExpect(jsonPath("$.data.content").value("新评论内容"));
    }

    @Test
    @DisplayName("创建评论 - taskId 从路径变量覆盖请求体")
    void createComment_shouldOverrideTaskIdFromPath() throws Exception {
        // Given
        CommentDto createdComment = new CommentDto(1L, 1L, "张三", "评论内容",
            Instant.now().toString());
        
        given(commentService.create(any(CreateCommentRequest.class))).willReturn(createdComment);

        // When & Then - 即使请求体中 taskId=999，也应该使用路径变量中的 taskId=1
        mockMvc.perform(post("/api/tasks/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"taskId\":999,\"author\":\"张三\",\"content\":\"评论内容\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.taskId").value(1));  // Should be 1, not 999
        
        // Verify the service received the correct taskId from path variable
        verify(commentService).create(argThat(request -> 
            request.taskId().equals(1L) && 
            request.author().equals("张三") && 
            request.content().equals("评论内容")
        ));
    }

    @Test
    @DisplayName("创建评论 - 当业务逻辑抛出 BizException 时")
    void createComment_shouldHandleBizException() throws Exception {
        // Given
        given(commentService.create(any(CreateCommentRequest.class)))
            .willThrow(new BizException(ErrorCode.PARAM_NOT_FOUND, "任务不存在"));

        // When & Then
        mockMvc.perform(post("/api/tasks/999/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"taskId\":999,\"author\":\"张三\",\"content\":\"评论内容\"}"))
            .andExpect(status().isInternalServerError());  // Note: 需要 GlobalExceptionHandler 来处理 BizException
    }

    // ==================== Delete Comment Tests ====================

    @Test
    @DisplayName("删除评论 - 应返回 204 NO_CONTENT")
    void deleteComment_shouldReturnStatus204() throws Exception {
        // Given
        doNothing().when(commentService).delete(1L);

        // When & Then
        mockMvc.perform(delete("/api/comments/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("删除评论 - 评论不存在时应抛出异常")
    void deleteComment_shouldThrowWhenNotFound() throws Exception {
        // Given
        doThrow(new BizException(ErrorCode.PARAM_NOT_FOUND, "评论不存在"))
            .when(commentService).delete(999L);

        // When & Then
        mockMvc.perform(delete("/api/comments/999")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());  // Note: 需要 GlobalExceptionHandler 来处理 BizException
    }

    // ==================== Additional Edge Cases ====================

    @Test
    @DisplayName("获取评论列表 - 限制 size 最大为 100")
    void getComments_shouldLimitSizeTo100_whenSizeExceeds100() throws Exception {
        // Given - Service should receive size=100 (capped), not 200
        Page<CommentDto> page = new PageImpl<>(List.of());
        given(commentService.findAll(eq(1L), eq(1), eq(100))).willReturn(page);

        // When & Then
        mockMvc.perform(get("/api/tasks/1/comments")
                .param("page", "1")
                .param("size", "200")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        
        // Verify that service received capped size of 100
        verify(commentService).findAll(eq(1L), eq(1), eq(100));
    }
}
