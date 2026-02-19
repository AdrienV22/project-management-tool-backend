package com.example.project_management_tool.controller;

import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.model.TaskModel.Priority;
import com.example.project_management_tool.repository.ProjectRepository;
import com.example.project_management_tool.repository.TaskRepository;
import com.example.project_management_tool.repository.UserRepository;
import com.example.project_management_tool.service.EmailService;
import com.example.project_management_tool.service.TaskHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TaskController.class)
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskRepository taskRepository;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private TaskHistoryService taskHistoryService;

    // -----------------------
    // GET /api/tasks (4 branches)
    // -----------------------

    @Test
    void getTasks_shouldReturn200_whenNoFilters() throws Exception {
        when(taskRepository.findAll()).thenReturn(List.of(new TaskModel()));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk());

        verify(taskRepository).findAll();
        verify(taskRepository, never()).findByStatus(anyString());
        verify(taskRepository, never()).findByProjectId(anyLong());
        verify(taskRepository, never()).findByProjectIdAndStatus(anyLong(), anyString());
    }

    @Test
    void getTasks_shouldReturn200_whenStatusOnly() throws Exception {
        when(taskRepository.findByStatus("EN_COURS")).thenReturn(List.of(new TaskModel()));

        mockMvc.perform(get("/api/tasks")
                        .param("status", "EN_COURS"))
                .andExpect(status().isOk());

        verify(taskRepository).findByStatus("EN_COURS");
        verify(taskRepository, never()).findAll();
        verify(taskRepository, never()).findByProjectId(anyLong());
        verify(taskRepository, never()).findByProjectIdAndStatus(anyLong(), anyString());
    }

    @Test
    void getTasks_shouldReturn200_whenProjectOnly() throws Exception {
        when(taskRepository.findByProjectId(1L)).thenReturn(List.of(new TaskModel()));

        mockMvc.perform(get("/api/tasks")
                        .param("projectId", "1"))
                .andExpect(status().isOk());

        verify(taskRepository).findByProjectId(1L);
        verify(taskRepository, never()).findAll();
        verify(taskRepository, never()).findByStatus(anyString());
        verify(taskRepository, never()).findByProjectIdAndStatus(anyLong(), anyString());
    }

    @Test
    void getTasks_shouldReturn200_whenStatusAndProject() throws Exception {
        when(taskRepository.findByProjectIdAndStatus(1L, "EN_ATTENTE"))
                .thenReturn(List.of(new TaskModel()));

        mockMvc.perform(get("/api/tasks")
                        .param("projectId", "1")
                        .param("status", "EN_ATTENTE"))
                .andExpect(status().isOk());

        verify(taskRepository).findByProjectIdAndStatus(1L, "EN_ATTENTE");
        verify(taskRepository, never()).findAll();
        verify(taskRepository, never()).findByStatus(anyString());
        verify(taskRepository, never()).findByProjectId(anyLong());
    }

    // -----------------------
    // GET /api/tasks/{taskId}
    // -----------------------

    @Test
    void getTaskById_shouldReturn200_whenFound() throws Exception {
        TaskModel t = new TaskModel();
        t.setId(6L);
        when(taskRepository.findById(6L)).thenReturn(Optional.of(t));

        mockMvc.perform(get("/api/tasks/6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6));

        verify(taskRepository).findById(6L);
    }

    @Test
    void getTaskById_shouldReturn404_whenNotFound() throws Exception {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound());

        verify(taskRepository).findById(999L);
    }

    // -----------------------
    // POST /api/tasks (branches erreurs + ok)
    // -----------------------

    @Test
    void createTask_shouldReturn400_whenTitleBlank() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "description": "Desc ok",
                                  "dueDate": "2026-02-28",
                                  "priority": "MOYENNE",
                                  "status": "En attente",
                                  "project": { "id": 1 },
                                  "targetUserId": 8
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Task title is required"));

        verify(taskRepository, never()).save(any());
        verify(projectRepository, never()).findById(anyLong());
    }

    @Test
    void createTask_shouldReturn400_whenProjectIdMissing() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Titre ok",
                                  "description": "Desc ok",
                                  "dueDate": "2026-02-28",
                                  "priority": "MOYENNE",
                                  "status": "En attente",
                                  "targetUserId": 8
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("project.id is required"));

        verify(taskRepository, never()).save(any());
        verify(projectRepository, never()).findById(anyLong());
    }

    @Test
    void createTask_shouldReturn400_whenProjectNotFound() throws Exception {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Titre ok",
                                  "description": "Desc ok",
                                  "dueDate": "2026-02-28",
                                  "priority": "MOYENNE",
                                  "status": "En attente",
                                  "project": { "id": 999 },
                                  "targetUserId": 8
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("project not found"));

        verify(taskRepository, never()).save(any());
        verify(projectRepository).findById(999L);
    }

    @Test
    void createTask_shouldReturn400_whenTargetUserNotFound() throws Exception {
        ProjectModel p = new ProjectModel();
        p.setId(1L);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(userRepository.existsById(999L)).thenReturn(false);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Titre ok",
                                  "description": "Desc ok",
                                  "dueDate": "2026-02-28",
                                  "priority": "MOYENNE",
                                  "status": "En attente",
                                  "project": { "id": 1 },
                                  "targetUserId": 999
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Target user not found"));

        verify(taskRepository, never()).save(any());
        verify(userRepository).existsById(999L);
    }

    @Test
    void createTask_shouldReturn201_whenOk_andSendEmail() throws Exception {
        ProjectModel p = new ProjectModel();
        p.setId(1L);
        p.setName("PMT Demo");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(userRepository.existsById(8L)).thenReturn(true);

        // userRepository.findById utilisé pour envoyer l'email
        com.example.project_management_tool.entity.User user = new com.example.project_management_tool.entity.User();
        user.setId(8L);
        user.setEmail("test1@example.com");
        when(userRepository.findById(8L)).thenReturn(Optional.of(user));

        TaskModel saved = new TaskModel();
        saved.setId(123L);
        saved.setTitle("Titre ok");
        saved.setProject(p);
        saved.setTargetUserId(8L);

        when(taskRepository.save(any(TaskModel.class))).thenReturn(saved);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Titre ok",
                                  "description": "Desc ok",
                                  "dueDate": "2026-02-28",
                                  "priority": "MOYENNE",
                                  "status": "En attente",
                                  "project": { "id": 1 },
                                  "targetUserId": 8
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(123))
                .andExpect(jsonPath("$.title").value("Titre ok"))
                .andExpect(jsonPath("$.targetUserId").value(8));

        verify(taskRepository).save(any(TaskModel.class));
        verify(emailService).sendTaskAssignedEmail(eq("test1@example.com"), eq("PMT Demo"), eq("Titre ok"));
    }

    // -----------------------
    // PUT /api/tasks/{taskId} (branches erreurs + ok)
    // -----------------------

    @Test
    void updateTask_shouldReturn404_whenTaskNotFound() throws Exception {
        when(taskRepository.findById(404L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/tasks/404")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Titre ok",
                                  "description": "Desc ok",
                                  "dueDate": "2026-02-28",
                                  "priority": "MOYENNE",
                                  "status": "En attente",
                                  "project": { "id": 1 },
                                  "targetUserId": 8
                                }
                                """))
                .andExpect(status().isNotFound());

        verify(taskRepository).findById(404L);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTask_shouldReturn400_whenTitleBlank() throws Exception {
        TaskModel existing = new TaskModel();
        existing.setId(6L);
        existing.setTitle("Ancien titre");
        existing.setTargetUserId(8L);

        when(taskRepository.findById(6L)).thenReturn(Optional.of(existing));

        mockMvc.perform(put("/api/tasks/6")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "description": "Desc ok",
                                  "dueDate": "2026-02-28",
                                  "priority": "MOYENNE",
                                  "status": "En attente",
                                  "project": { "id": 1 },
                                  "targetUserId": 8
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Task title is required"));

        verify(taskRepository, never()).save(any());
        verify(taskHistoryService, never()).recordChange(any(), anyString(), any(), any(), anyString());
    }

    @Test
    void updateTask_shouldReturn400_whenProjectNotFound() throws Exception {
        TaskModel existing = new TaskModel();
        existing.setId(6L);
        existing.setTitle("Ancien titre");
        existing.setTargetUserId(8L);
        ProjectModel oldP = new ProjectModel();
        oldP.setId(1L);
        existing.setProject(oldP);

        when(taskRepository.findById(6L)).thenReturn(Optional.of(existing));
        when(userRepository.existsById(8L)).thenReturn(true);
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/tasks/6")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Titre ok",
                                  "description": "Desc ok",
                                  "dueDate": "2026-02-28",
                                  "priority": "MOYENNE",
                                  "status": "En attente",
                                  "project": { "id": 999 },
                                  "targetUserId": 8
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("project not found"));

        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTask_shouldReturn400_whenTargetUserNotFound() throws Exception {
        TaskModel existing = new TaskModel();
        existing.setId(6L);
        existing.setTitle("Ancien titre");

        when(taskRepository.findById(6L)).thenReturn(Optional.of(existing));
        when(userRepository.existsById(999L)).thenReturn(false);

        mockMvc.perform(put("/api/tasks/6")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Titre ok",
                                  "description": "Desc ok",
                                  "dueDate": "2026-02-28",
                                  "priority": "MOYENNE",
                                  "status": "En attente",
                                  "project": { "id": 1 },
                                  "targetUserId": 999
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Target user not found"));

        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTask_shouldReturn200_whenOk_andSendEmailWhenAssigneeChanged() throws Exception {
        // existing task
        ProjectModel oldP = new ProjectModel();
        oldP.setId(1L);
        oldP.setName("PMT Demo");

        TaskModel existing = new TaskModel();
        existing.setId(6L);
        existing.setTitle("Ancien titre");
        existing.setDescription("Ancienne desc");
        existing.setDueDate(LocalDate.of(2026, 2, 28));
        existing.setStatus("En attente");
        existing.setPriority(Priority.MOYENNE);
        existing.setProject(oldP);
        existing.setTargetUserId(7L); // old user

        when(taskRepository.findById(6L)).thenReturn(Optional.of(existing));

        // checks in controller
        when(userRepository.existsById(8L)).thenReturn(true);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(oldP));

        // email: controller does userRepository.findById(newTarget)
        com.example.project_management_tool.entity.User newUser = new com.example.project_management_tool.entity.User();
        newUser.setId(8L);
        newUser.setEmail("test1@example.com");
        when(userRepository.findById(8L)).thenReturn(Optional.of(newUser));

        // save returns updated entity
        when(taskRepository.save(any(TaskModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/tasks/6")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Titre ok",
                                  "description": "Desc ok",
                                  "dueDate": "2026-02-28",
                                  "priority": "MOYENNE",
                                  "status": "En cours",
                                  "project": { "id": 1 },
                                  "targetUserId": 8
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.title").value("Titre ok"))
                .andExpect(jsonPath("$.targetUserId").value(8));

        verify(taskHistoryService, atLeastOnce()).recordChange(any(), anyString(), any(), any(), eq("API"));
        verify(emailService).sendTaskAssignedEmail(eq("test1@example.com"), eq("PMT Demo"), eq("Titre ok"));
    }

    // -----------------------
    // DELETE /api/tasks/{taskId}
    // -----------------------

    @Test
    void deleteTask_shouldReturn404_whenNotExists() throws Exception {
        when(taskRepository.existsById(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/tasks/999"))
                .andExpect(status().isNotFound());

        verify(taskRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteTask_shouldReturn204_whenExists() throws Exception {
        when(taskRepository.existsById(9L)).thenReturn(true);

        mockMvc.perform(delete("/api/tasks/9"))
                .andExpect(status().isNoContent());

        verify(taskRepository).deleteById(9L);
    }
}
