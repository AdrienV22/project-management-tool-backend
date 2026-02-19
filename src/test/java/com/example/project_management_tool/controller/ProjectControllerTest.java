package com.example.project_management_tool.controller;

import com.example.project_management_tool.entity.ProjectMember;
import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.repository.ProjectMemberRepository;
import com.example.project_management_tool.repository.ProjectRepository;
import com.example.project_management_tool.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
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

    @MockBean
    private ProjectMemberRepository projectMemberRepository;

    // -----------------------
    // CRUD Projects
    // -----------------------

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

        verify(projectRepository, never()).deleteById(anyLong());
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
                        .content("""
                                {
                                  "name":"New",
                                  "description":"Desc",
                                  "startDate":"2026-01-01"
                                }
                                """))
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
                        .content("""
                                {
                                  "name":"New",
                                  "description":"Desc",
                                  "startDate":"2026-01-01"
                                }
                                """))
                .andExpect(status().isOk());

        verify(projectRepository).save(any(ProjectModel.class));
    }

    @Test
    void createProject_shouldReturn400_whenMissingName() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description":"Desc",
                                  "startDate":"2026-01-01"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(projectRepository, never()).save(any(ProjectModel.class));
    }

    @Test
    void createProject_shouldReturn400_whenMissingStartDate() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"P1",
                                  "description":"Desc"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(projectRepository, never()).save(any(ProjectModel.class));
    }

    @Test
    void createProject_shouldReturn201_whenOk() throws Exception {
        when(projectRepository.save(any(ProjectModel.class))).thenReturn(new ProjectModel());

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"P1",
                                  "description":"Desc",
                                  "startDate":"2026-01-01"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(projectRepository).save(any(ProjectModel.class));
    }

    // -----------------------
    // Members (inviter + rôles)
    // -----------------------

    @Test
    void addOrUpdateUserInProject_shouldReturn400_whenBodyInvalid_missingEmailOrRole() throws Exception {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(new ProjectModel()));

        mockMvc.perform(put("/api/projects/1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"MEMBRE\"}"))
                .andExpect(status().isBadRequest());

        verify(userRepository, never()).findByEmail(anyString());
        verify(projectMemberRepository, never()).save(any());
    }

    @Test
    void addOrUpdateUserInProject_shouldReturn404_whenProjectNotFound() throws Exception {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/projects/1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@test.com\",\"role\":\"MEMBRE\"}"))
                .andExpect(status().isNotFound());

        verify(userRepository, never()).findByEmail(anyString());
        verify(projectMemberRepository, never()).save(any());
    }

    @Test
    void addOrUpdateUserInProject_shouldReturn400_whenUserNotFoundForEmail() throws Exception {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(new ProjectModel()));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/projects/1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@test.com\",\"role\":\"MEMBRE\"}"))
                .andExpect(status().isBadRequest());

        verify(projectMemberRepository, never()).save(any());
    }

    @Test
    void addOrUpdateUserInProject_shouldReturn400_whenRoleInvalid() throws Exception {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(new ProjectModel()));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(new User()));

        mockMvc.perform(put("/api/projects/1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@test.com\",\"role\":\"INVALID\"}"))
                .andExpect(status().isBadRequest());

        verify(projectMemberRepository, never()).save(any());
    }

    @Test
    void addOrUpdateUserInProject_shouldReturn200_whenInviteOrUpdateOk() throws Exception {
        ProjectModel project = new ProjectModel();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        User user = new User();
        user.setId(10L);
        user.setEmail("user@test.com");
        user.setUsername("adrien");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        when(projectMemberRepository.findByProject_IdAndUser_Id(1L, 10L))
                .thenReturn(Optional.empty());

        ProjectMember saved = mock(ProjectMember.class);
        when(saved.getUser()).thenReturn(user);
        when(saved.getRole()).thenReturn(ProjectMember.ProjectRole.MEMBRE);
        when(saved.getJoinedAt()).thenReturn(LocalDateTime.now());
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(saved);

        mockMvc.perform(put("/api/projects/1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@test.com\",\"role\":\"MEMBRE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.username").value("adrien"))
                .andExpect(jsonPath("$.role").value("MEMBRE"));

        verify(projectMemberRepository).save(any(ProjectMember.class));
    }

    @Test
    void listProjectMembers_shouldReturn404_whenProjectNotFound() throws Exception {
        when(projectRepository.existsById(1L)).thenReturn(false);

        mockMvc.perform(get("/api/projects/1/users"))
                .andExpect(status().isNotFound());

        verify(projectMemberRepository, never()).findByProject_Id(anyLong());
    }

    @Test
    void listProjectMembers_shouldReturn200_withMembers() throws Exception {
        when(projectRepository.existsById(1L)).thenReturn(true);

        User user = new User();
        user.setId(10L);
        user.setEmail("user@test.com");
        user.setUsername("adrien");

        ProjectMember pm = mock(ProjectMember.class);
        when(pm.getUser()).thenReturn(user);
        when(pm.getRole()).thenReturn(ProjectMember.ProjectRole.ADMIN);
        when(pm.getJoinedAt()).thenReturn(LocalDateTime.now());

        when(projectMemberRepository.findByProject_Id(1L)).thenReturn(List.of(pm));

        mockMvc.perform(get("/api/projects/1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(10))
                .andExpect(jsonPath("$[0].email").value("user@test.com"))
                .andExpect(jsonPath("$[0].username").value("adrien"))
                .andExpect(jsonPath("$[0].role").value("ADMIN"));

        verify(projectMemberRepository).findByProject_Id(1L);
    }
}
