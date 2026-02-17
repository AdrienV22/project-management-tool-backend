package com.example.project_management_tool.controller;

import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.repository.ProjectRepository;
import com.example.project_management_tool.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private UserRepository userRepository;

    @Test
    void getAllProjects_shouldReturn200() throws Exception {
        doReturn(List.of(new ProjectModel()))
                .when(projectRepository).findAll();

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk());
    }

    @Test
    void getProjectById_shouldReturn200_whenFound() throws Exception {
        doReturn(Optional.of(new ProjectModel()))
                .when(projectRepository).findById(1L);

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getProjectById_shouldReturn404_whenNotFound() throws Exception {
        doReturn(Optional.empty())
                .when(projectRepository).findById(1L);

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProject_shouldReturn404_whenNotExists() throws Exception {
        doReturn(false)
                .when(projectRepository).existsById(1L);

        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isNotFound());

        verify(projectRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteProject_shouldReturn204_whenExists() throws Exception {
        doReturn(true)
                .when(projectRepository).existsById(1L);

        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isNoContent());

        verify(projectRepository).deleteById(1L);
    }

    @Test
    void updateProject_shouldReturn404_whenNotFound() throws Exception {
        doReturn(Optional.empty())
                .when(projectRepository).findById(1L);

        mockMvc.perform(put("/api/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New\",\"description\":\"Desc\"}"))
                .andExpect(status().isNotFound());

        verify(projectRepository, never()).save(any(ProjectModel.class));
    }

    @Test
    void updateProject_shouldReturn200_whenFound() throws Exception {
        ProjectModel existing = new ProjectModel();
        existing.setName("Old");

        doReturn(Optional.of(existing))
                .when(projectRepository).findById(1L);

        doReturn(existing)
                .when(projectRepository).save(any(ProjectModel.class));

        // Payload minimal : on n'envoie que name/description pour éviter les soucis de format date/enum
        mockMvc.perform(put("/api/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New\",\"description\":\"Desc\"}"))
                .andExpect(status().isOk());

        verify(projectRepository).save(any(ProjectModel.class));
    }

    @Test
    void addUserToProject_shouldReturn404_whenProjectNotFound() throws Exception {
        doReturn(Optional.empty())
                .when(projectRepository).findById(1L);

        mockMvc.perform(put("/api/projects/1/users")
                        .param("userEmail", "u@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addUserToProject_shouldReturn400_whenUserNotFound() throws Exception {
        ProjectModel project = new ProjectModel();
        if (project.getUserList() == null) {
            project.setUserList(new ArrayList<>());
        }

        doReturn(Optional.of(project))
                .when(projectRepository).findById(1L);

        doReturn(Optional.empty())
                .when(userRepository).findByEmail("u@example.com");

        mockMvc.perform(put("/api/projects/1/users")
                        .param("userEmail", "u@example.com"))
                .andExpect(status().isBadRequest());

        verify(projectRepository, never()).save(any(ProjectModel.class));
    }

    @Test
    void addUserToProject_shouldReturn200_whenOk() throws Exception {
        ProjectModel project = new ProjectModel();
        if (project.getUserList() == null) {
            project.setUserList(new ArrayList<>());
        }

        User user = new User("u", "u@example.com", "pwd", User.UserRole.ADMIN);

        doReturn(Optional.of(project))
                .when(projectRepository).findById(1L);

        doReturn(Optional.of(user))
                .when(userRepository).findByEmail("u@example.com");

        doReturn(project)
                .when(projectRepository).save(any(ProjectModel.class));

        mockMvc.perform(put("/api/projects/1/users")
                        .param("userEmail", "u@example.com"))
                .andExpect(status().isOk());

        verify(projectRepository).save(any(ProjectModel.class));
    }

    @Test
    void createProject_shouldReturn400_whenAuthUserNotFound() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("john@example.com");

        doReturn(Optional.empty())
                .when(userRepository).findByEmail("john@example.com");

        mockMvc.perform(post("/api/projects")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"P1\"}"))
                .andExpect(status().isBadRequest());

        verify(projectRepository, never()).save(any(ProjectModel.class));
    }

    @Test
    void createProject_shouldReturn200_whenOk() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("john@example.com");

        User user = new User("john", "john@example.com", "pwd", User.UserRole.ADMIN);

        doReturn(Optional.of(user))
                .when(userRepository).findByEmail("john@example.com");

        doReturn(new ProjectModel())
                .when(projectRepository).save(any(ProjectModel.class));

        mockMvc.perform(post("/api/projects")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"P1\",\"description\":\"Desc\"}"))
                .andExpect(status().isOk());

        verify(projectRepository).save(any(ProjectModel.class));
    }
}
