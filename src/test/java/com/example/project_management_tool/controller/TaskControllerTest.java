package com.example.project_management_tool.controller;

import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.repository.ProjectRepository;
import com.example.project_management_tool.repository.TaskRepository;
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

@WebMvcTest(controllers = TaskController.class)
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskRepository taskRepository;

    @MockBean
    private ProjectRepository projectRepository;

    @Test
    void getAllTasks_shouldReturn200() throws Exception {
        when(taskRepository.findAll()).thenReturn(List.of(new TaskModel()));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk());
    }

    @Test
    void createTask_shouldReturn400_whenMissingProjectId() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"T1\"}"))
                .andExpect(status().isBadRequest());

        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_shouldReturn400_whenProjectNotFound() throws Exception {
        when(projectRepository.findById(10L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"T1\",\"project\":{\"id\":10}}"))
                .andExpect(status().isBadRequest());

        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_shouldReturn201_whenOk() throws Exception {
        ProjectModel project = new ProjectModel();
        project.setId(10L);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(taskRepository.save(any(TaskModel.class))).thenReturn(new TaskModel());

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"T1\",\"project\":{\"id\":10}}"))
                .andExpect(status().isCreated());

        verify(taskRepository).save(any(TaskModel.class));
    }

    @Test
    void updateTask_shouldReturn404_whenNotFound() throws Exception {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New\"}"))
                .andExpect(status().isNotFound());

        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTask_shouldReturn200_whenFound() throws Exception {
        TaskModel existing = new TaskModel();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(TaskModel.class))).thenReturn(existing);

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New\",\"status\":\"DONE\"}"))
                .andExpect(status().isOk());

        verify(taskRepository).save(any(TaskModel.class));
    }

    @Test
    void deleteTask_shouldReturn404_whenNotFound() throws Exception {
        when(taskRepository.existsById(5L)).thenReturn(false);

        mockMvc.perform(delete("/api/tasks/5"))
                .andExpect(status().isNotFound());

        verify(taskRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteTask_shouldReturn204_whenFound() throws Exception {
        when(taskRepository.existsById(5L)).thenReturn(true);

        mockMvc.perform(delete("/api/tasks/5"))
                .andExpect(status().isNoContent());

        verify(taskRepository).deleteById(5L);
    }
}
