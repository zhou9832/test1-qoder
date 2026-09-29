package com.taskboard.controller;

import com.taskboard.entity.Project;
import com.taskboard.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for ProjectController.
 */
@SpringBootTest
@Transactional  // Ensures each test rolls back, leaving DB clean after each test
public class ProjectControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProjectRepository projectRepository;

    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void testGetAllProjects() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", is(not(empty()))));
    }

    @Test
    void testGetProjectById() throws Exception {
        // First get all projects to find the first project's ID
        MvcResult result = mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        // Extract the first project's ID from response and verify it exists
        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateProject() throws Exception {
        String name = "新测试项目";
        String description = "这是一个测试项目描述";

        mockMvc.perform(post("/api/projects")
                        .param("name", name)
                        .param("description", description))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value(name))
                .andExpect(jsonPath("$.data.description").value(description));
    }

    @Test
    void testUpdateProject() throws Exception {
        String newName = "更新后的项目名称";

        mockMvc.perform(put("/api/projects/2")
                        .param("name", newName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value(newName));
    }

    @Test
    void testDeleteProject() throws Exception {
        mockMvc.perform(delete("/api/projects/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify it's deleted
        mockMvc.perform(get("/api/projects/3"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateProjectWithDuplicateName() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .param("name", "教程研发"))
                .andExpect(status().isConflict());
    }

    @Test
    void testCreateProjectWithoutDescription() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .param("name", "无描述项目"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("无描述项目"));
    }
}
