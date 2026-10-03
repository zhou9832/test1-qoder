package com.taskboard.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.taskboard.service.ProjectService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProjectControllerTest {

    private MockMvc mockMvc;

    private ProjectService projectService = org.mockito.Mockito.mock(ProjectService.class);

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new ProjectController(projectService))
            .build();
    }

    @Test
    public void shouldGetAllProjects() throws Exception {
        given(projectService.getAllProjects())
            .willReturn(java.util.List.of());

        mockMvc.perform(get("/api/projects"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    public void shouldCreateProject() throws Exception {
        given(projectService.createProject(any()))
            .willReturn(new com.taskboard.dto.ProjectDto(
                1L, "测试项目", "测试描述", 
                java.time.Instant.now().toString(), java.time.Instant.now().toString()
            ));

        mockMvc.perform(post("/api/projects")
                .param("name", "测试项目")
                .param("description", "测试描述"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.name").value("测试项目"));
    }

    @Test
    public void shouldGetProjectById() throws Exception {
        given(projectService.getProjectById(1L))
            .willReturn(java.util.Optional.of(new com.taskboard.dto.ProjectDto(
                1L, "测试项目", "测试描述", 
                java.time.Instant.now().toString(), java.time.Instant.now().toString()
            )));

        mockMvc.perform(get("/api/projects/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.name").value("测试项目"));
    }

    @Test
    public void shouldReturnNotFoundWhenProjectDoesNotExist() throws Exception {
        given(projectService.getProjectById(999L))
            .willReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/projects/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldUpdateProject() throws Exception {
        given(projectService.updateProject(eq(1L), any()))
            .willReturn(new com.taskboard.dto.ProjectDto(
                1L, "更新后", "新描述", 
                java.time.Instant.now().toString(), java.time.Instant.now().toString()
            ));

        mockMvc.perform(put("/api/projects/1")
                .param("name", "更新后")
                .param("description", "新描述"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.name").value("更新后"));
    }

    @Test
    public void shouldDeleteProject() throws Exception {
        doNothing().when(projectService).deleteProject(1L);

        mockMvc.perform(delete("/api/projects/1"))
            .andExpect(status().isNoContent());
    }
}
