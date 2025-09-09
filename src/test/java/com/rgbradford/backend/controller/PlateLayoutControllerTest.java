package com.rgbradford.backend.controller;

import com.rgbradford.backend.dto.request.CreatePlateLayoutRequest;
import com.rgbradford.backend.entity.PlateLayout;
import com.rgbradford.backend.entity.Project;
import com.rgbradford.backend.entity.User;
import com.rgbradford.backend.repository.PlateLayoutRepository;
import com.rgbradford.backend.repository.ProjectRepository;
import com.rgbradford.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PlateLayoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PlateLayoutRepository plateLayoutRepository;

    private User testUser;
    private Project testProject;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        plateLayoutRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        // Create a test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);

        // Create a test project
        testProject = new Project();
        testProject.setName("Test Project");
        testProject.setUser(testUser);
        testProject = projectRepository.save(testProject);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createPlateLayout_ValidRequest_ReturnsCreated() throws Exception {
        CreatePlateLayoutRequest request = new CreatePlateLayoutRequest();
        request.setRows(8);
        request.setColumns(12);
        request.setProjectId(testProject.getId());

        mockMvc.perform(post("/api/plate-layouts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rows").value(8))
                .andExpect(jsonPath("$.columns").value(12))
                .andExpect(jsonPath("$.projectId").value(testProject.getId()));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createPlateLayout_ProjectAlreadyHasLayout_ReturnsBadRequest() throws Exception {
        // First layout
        CreatePlateLayoutRequest request1 = new CreatePlateLayoutRequest();
        request1.setRows(8);
        request1.setColumns(12);
        request1.setProjectId(testProject.getId());
        
        mockMvc.perform(post("/api/plate-layouts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        // Second layout for same project
        CreatePlateLayoutRequest request2 = new CreatePlateLayoutRequest();
        request2.setRows(16);
        request2.setColumns(24);
        request2.setProjectId(testProject.getId());

        mockMvc.perform(post("/api/plate-layouts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createPlateLayout_InvalidDimensions_ReturnsBadRequest() throws Exception {
        CreatePlateLayoutRequest request = new CreatePlateLayoutRequest();
        request.setRows(0);  // Invalid: less than minimum
        request.setColumns(100);  // Invalid: more than maximum
        request.setProjectId(testProject.getId());

        mockMvc.perform(post("/api/plate-layouts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "nonexistent@example.com")
    void createPlateLayout_ProjectNotFound_ReturnsNotFound() throws Exception {
        CreatePlateLayoutRequest request = new CreatePlateLayoutRequest();
        request.setRows(8);
        request.setColumns(12);
        request.setProjectId(999L);  // Non-existent project ID

        mockMvc.perform(post("/api/plate-layouts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
