package com.example.project_management_tool.controller;

import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.repository.ProjectRepository;
import com.example.project_management_tool.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProjectController.class)
@AutoConfigureMockMvc
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private UserRepository userRepository;

    @Test
    void getAllProjects_shouldReturn200() throws Exception {
        when(projectRepository.findAll()).thenReturn(List.of(new ProjectModel()));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk());
    }

    @Test
    void getProjectById_shouldReturn200_whenFound() throws Exception {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(new ProjectModel()));

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getProjectById_shouldReturn404_whenNotFound() throws Exception {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProject_shouldReturn404_whenNotExists() throws Exception {
        when(projectRepository.existsById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isNotFound());

        verify(projectRepository, never()).deleteById(any(Long.class));
    }

    @Test
    void deleteProject_shouldReturn204_whenExists() throws Exception {
        when(projectRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isNoContent());

        verify(projectRepository).deleteById(1L);
    }

    @Test
    void updateProject_shouldReturn404_whenNotFound() throws Exception {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New\",\"description\":\"Desc\"}"))
                .andExpect(status().isNotFound());

        verify(projectRepository, never()).save(any(ProjectModel.class));
    }

    @Test
    void updateProject_shouldReturn200_whenFound() throws Exception {
        ProjectModel existing = new ProjectModel();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(projectRepository.save(any(ProjectModel.class))).thenReturn(existing);

        mockMvc.perform(put("/api/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New\",\"description\":\"Desc\"}"))
                .andExpect(status().isOk());

        verify(projectRepository).save(any(ProjectModel.class));
    }

    @Test
    void createProject_shouldReturn400_whenMissingName() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Desc\"}"))
                .andExpect(status().isBadRequest());

        verify(projectRepository, never()).save(any(ProjectModel.class));
    }

    @Test
    void createProject_shouldReturn201_whenOk() throws Exception {
        when(projectRepository.save(any(ProjectModel.class))).thenReturn(new ProjectModel());

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"P1\",\"description\":\"Desc\"}"))
                .andExpect(status().isCreated());

        verify(projectRepository).save(any(ProjectModel.class));
    }
}
