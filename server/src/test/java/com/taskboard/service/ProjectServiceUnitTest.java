package com.taskboard.service;

import com.taskboard.dto.CreateProjectRequest;
import com.taskboard.dto.ProjectDto;
import com.taskboard.dto.UpdateProjectRequest;
import com.taskboard.entity.Project;
import com.taskboard.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProjectService business logic validation.
 */
@SpringBootTest
@ActiveProfiles("test")
class ProjectServiceUnitTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        projectRepository.deleteAll();
    }

    // ==================== Create Project Tests ====================

    @Test
    @DisplayName("创建项目 - 正常路径应返回 ProjectDto")
    void createProject_shouldReturnDto_whenNameIsValid() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest("测试项目", "这是一个测试项目");

        // When
        ProjectDto result = projectService.createProject(request);

        // Then
        assertNotNull(result);
        assertEquals("测试项目", result.name());
        assertEquals("这是一个测试项目", result.description());
        assertNotNull(result.id());
        assertNotNull(result.createdAt());
    }

    @Test
    @DisplayName("创建项目 - name 为空字符串应抛 ResponseStatusException")
    void createProject_shouldFailWhenNameIsEmpty() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest("", "描述");

        // When & Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectService.createProject(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("cannot be blank"));
    }

    @Test
    @DisplayName("创建项目 - name 为空白字符应抛 ResponseStatusException")
    void createProject_shouldFailWhenNameIsBlank() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest("   ", "描述");

        // When & Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectService.createProject(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("cannot be blank"));
    }

    @Test
    @DisplayName("创建项目 - name 超过 64 字符应抛 ResponseStatusException")
    void createProject_shouldFailWhenNameExceedsLimit() {
        // Given
        String longName = "a".repeat(65);
        CreateProjectRequest request = new CreateProjectRequest(longName, "描述");

        // When & Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectService.createProject(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("64 characters or less"));
    }

    @Test
    @DisplayName("创建项目 - description 超过 512 字符应抛 ResponseStatusException")
    void createProject_shouldFailWhenDescriptionExceedsLimit() {
        // Given
        String validName = "有效名称";
        String longDesc = "b".repeat(513);
        CreateProjectRequest request = new CreateProjectRequest(validName, longDesc);

        // When & Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectService.createProject(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("512 characters or less"));
    }

    @Test
    @DisplayName("创建项目 - name 重复应抛 ResponseStatusException")
    void createProject_shouldFailWhenNameDuplicate() {
        // Given
        String existingName = "已存在的项目";
        
        // 预置一个已有项目
        CreateProjectRequest firstRequest = new CreateProjectRequest(existingName, "第一个项目");
        projectService.createProject(firstRequest);

        // 尝试创建同名项目
        CreateProjectRequest duplicateRequest = new CreateProjectRequest(existingName, "重复项目");

        // When & Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectService.createProject(duplicateRequest)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("already exists"));
    }

    // ==================== Update Project Tests ====================

    @Test
    @DisplayName("更新项目 - 不存在的 ID 应抛 ResponseStatusException")
    void updateProject_shouldFailWhenIdNotFound() {
        // Given
        Long nonExistentId = 999L;
        UpdateProjectRequest request = new UpdateProjectRequest("新名称", "新描述");

        // When & Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectService.updateProject(nonExistentId, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("not found"));
    }

    @Test
    @DisplayName("更新项目 - 正常路径应返回 ProjectDto")
    void updateProject_shouldSuccess_whenNameAndDescProvided() {
        // Given
        CreateProjectRequest createRequest = new CreateProjectRequest("原始名称", "原始描述");
        ProjectDto created = projectService.createProject(createRequest);

        UpdateProjectRequest updateRequest = new UpdateProjectRequest("更新后的名称", "更新后的描述");

        // When
        ProjectDto result = projectService.updateProject(created.id(), updateRequest);

        // Then
        assertEquals("更新后的名称", result.name());
        assertEquals("更新后的描述", result.description());
        assertEquals(created.id(), result.id());
    }

    @Test
    @DisplayName("更新项目 - 仅更新名称应成功")
    void updateProject_shouldUpdateNameOnly() {
        // Given
        CreateProjectRequest createRequest = new CreateProjectRequest("原始名称", "原始描述");
        ProjectDto created = projectService.createProject(createRequest);

        UpdateProjectRequest updateRequest = new UpdateProjectRequest("新名称", null);

        // When
        ProjectDto result = projectService.updateProject(created.id(), updateRequest);

        // Then
        assertEquals("新名称", result.name());
        assertEquals("原始描述", result.description());
    }

    @Test
    @DisplayName("更新项目 - 仅更新描述应成功")
    void updateProject_shouldUpdateDescriptionOnly() {
        // Given
        CreateProjectRequest createRequest = new CreateProjectRequest("原始名称", "原始描述");
        ProjectDto created = projectService.createProject(createRequest);

        UpdateProjectRequest updateRequest = new UpdateProjectRequest(null, "新描述");

        // When
        ProjectDto result = projectService.updateProject(created.id(), updateRequest);

        // Then
        assertEquals("原始名称", result.name());
        assertEquals("新描述", result.description());
    }

    @Test
    @DisplayName("更新项目 - name 超出长度限制应抛异常")
    void updateProject_shouldFailWhenNameExceedsLimit() {
        // Given
        CreateProjectRequest createRequest = new CreateProjectRequest("原始名称", "描述");
        ProjectDto created = projectService.createProject(createRequest);

        String longName = "a".repeat(65);
        UpdateProjectRequest updateRequest = new UpdateProjectRequest(longName, null);

        // When & Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectService.updateProject(created.id(), updateRequest)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("64 characters or less"));
    }

    @Test
    @DisplayName("更新项目 - name 重复但排除当前项目应成功")
    void updateProject_shouldSuccessWhenNameMatchesOwn() {
        // Given
        CreateProjectRequest createRequest = new CreateProjectRequest("原名称", "描述");
        ProjectDto created = projectService.createProject(createRequest);

        // 创建另一个不同名称的项目
        CreateProjectRequest anotherRequest = new CreateProjectRequest("其他项目", "描述");
        projectService.createProject(anotherRequest);

        // 将当前项目改回"原名称"（模拟先改名再改回来）
        UpdateProjectRequest updateRequest = new UpdateProjectRequest("原名称", null);

        // When & Then - 不应抛出重复错误
        ProjectDto result = projectService.updateProject(created.id(), updateRequest);
        assertEquals("原名称", result.name());
    }

    // ==================== Delete Project Tests ====================

    @Test
    @DisplayName("删除项目 - 不存在的 ID 应抛 ResponseStatusException")
    void deleteProject_shouldFailWhenIdNotFound() {
        // Given
        Long nonExistentId = 999L;

        // When & Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectService.deleteProject(nonExistentId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("not found"));
    }

    @Test
    @DisplayName("删除项目 - 正常路径应成功")
    void deleteProject_shouldSucceed_whenProjectExists() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest("待删除项目", "描述");
        ProjectDto created = projectService.createProject(request);

        assertTrue(projectRepository.existsById(created.id()));

        // When
        projectService.deleteProject(created.id());

        // Then
        assertFalse(projectRepository.existsById(created.id()));
    }

    // ==================== Get Project Tests ====================

    @Test
    @DisplayName("获取所有项目 - 空列表应返回空数组")
    void getAllProjects_shouldReturnEmptyList_whenNoProjects() {
        // When
        var result = projectService.getAllProjects();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("获取所有项目 - 多个项目应按创建时间倒序返回")
    void getAllProjects_shouldOrderByCreatedAtDesc() {
        // Given
        CreateProjectRequest first = new CreateProjectRequest("项目1", "描述1");
        projectService.createProject(first);

        CreateProjectRequest second = new CreateProjectRequest("项目2", "描述2");
        projectService.createProject(second);

        // When
        var result = projectService.getAllProjects();

        // Then
        assertEquals(2, result.size());
        assertEquals("项目2", result.get(0).name());
        assertEquals("项目1", result.get(1).name());
    }

    @Test
    @DisplayName("获取项目ID - 存在的ID应返回Optional")
    void getProjectById_shouldReturnPresent_whenExists() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest("测试项目", "描述");
        ProjectDto created = projectService.createProject(request);

        // When
        Optional<ProjectDto> result = projectService.getProjectById(created.id());

        // Then
        assertTrue(result.isPresent());
        assertEquals(created.name(), result.get().name());
    }

    @Test
    @DisplayName("获取项目ID - 不存在的ID应返回Optional.empty")
    void getProjectById_shouldReturnEmpty_whenNotExists() {
        // When
        Optional<ProjectDto> result = projectService.getProjectById(999L);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("存在性检查 - 存在的ID应返回true")
    void existsById_shouldReturnTrue_whenExists() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest("测试项目", "描述");
        ProjectDto created = projectService.createProject(request);

        // When & Then
        assertTrue(projectService.existsById(created.id()));
    }

    @Test
    @DisplayName("存在性检查 - 不存在的ID应返回false")
    void existsById_shouldReturnFalse_whenNotExists() {
        // When & Then
        assertFalse(projectService.existsById(999L));
    }
}
