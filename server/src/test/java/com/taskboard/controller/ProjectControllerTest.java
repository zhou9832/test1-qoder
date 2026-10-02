package com.taskboard.controller;

import com.taskboard.entity.Project;
import com.taskboard.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import com.taskboard.service.ProjectService;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

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
        Project mockProject = new Project("测试项目", "测试描述");
        mockProject.setId(1L);
        
        given(projectService.createProject(any()))
            .willReturn(new com.taskboard.dto.ProjectDto(
                1L, "测试项目", "测试描述", 
                "2026-01-01T00:00:00Z", "2026-01-01T00:00:00Z"
            ));

        mockMvc.perform(post("/api/projects")
                .param("name", "测试项目")
                .param("description", "测试描述"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.name").value("测试项目"));
    }

    @Test
    public void shouldGetProjectById() throws Exception {
        given(projectService.getProjectById(1L))
            .thenReturn(java.util.Optional.of(new com.taskboard.dto.ProjectDto(
                1L, "测试项目", "测试描述",
                "2026-01-01T00:00:00Z", "2026-01-01T00:00:00Z"
            )));

        mockMvc.perform(get("/api/projects/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.name").value("测试项目"));
    }

    @Test
    public void shouldUpdateProject() throws Exception {
        given(projectService.updateProject(any(), any()))
            .willReturn(new com.taskboard.dto.ProjectDto(
                1L, "更新后的名称", "更新后的描述",
                "2026-01-01T00:00:00Z", "2026-01-01T00:00:00Z"
            ));

        mockMvc.perform(put("/api/projects/1")
                .param("name", "更新后的名称")
                .param("description", "更新后的描述"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.name").value("更新后的名称"));
    }

    @Test
    public void shouldDeleteProject() throws Exception {
        doNothing().when(projectService).deleteProject(1L);

        mockMvc.perform(delete("/api/projects/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }
}
