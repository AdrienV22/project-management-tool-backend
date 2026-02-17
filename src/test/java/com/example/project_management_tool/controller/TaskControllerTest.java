package com.example.project_management_tool.controller;

import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.repository.ProjectRepository;
import com.example.project_management_tool.repository.TaskRepository;
import com.example.project_management_tool.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock TaskRepository taskRepository;
    @Mock ProjectRepository projectRepository;
    @Mock UserRepository userRepository;

    @Mock Authentication authentication;

    TaskController taskController;

    @BeforeEach
    void setup() {
        taskController = new TaskController(taskRepository, projectRepository, userRepository);
    }

    @Test
    void getAllTasks_shouldReturnList() {
        when(taskRepository.findAll()).thenReturn(List.of(new TaskModel(), new TaskModel()));

        List<TaskModel> result = taskController.getAllTasks();

        assertEquals(2, result.size());
        verify(taskRepository).findAll();
    }

    @Test
    void createTask_shouldSaveTask_whenUserAndProjectExist() {
        // Given
        when(authentication.getName()).thenReturn("user@test.com");

        User user = new User();
        user.setEmail("user@test.com");
        user.setUserRole(User.UserRole.MEMBRE);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        ProjectModel project = new ProjectModel();
        project.setId(10L);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        TaskModel task = new TaskModel();
        ProjectModel projectRef = new ProjectModel();
        projectRef.setId(10L);
        task.setProject(projectRef);

        TaskModel saved = new TaskModel();
        saved.setProject(project);
        when(taskRepository.save(any(TaskModel.class))).thenReturn(saved);

        // When
        TaskModel result = taskController.createTask(task, authentication);

        // Then
        assertNotNull(result);
        verify(userRepository).findByEmail("user@test.com");
        verify(projectRepository).findById(10L);
        verify(taskRepository).save(any(TaskModel.class));
    }

    @Test
    void updateTask_shouldUpdateFields_andSave() {
        // Given
        Long taskId = 1L;

        TaskModel existing = new TaskModel();
        existing.setTitle("Old");
        existing.setDescription("Old desc");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(TaskModel.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskModel updated = new TaskModel();
        updated.setTitle("New");
        updated.setDescription("New desc");
        updated.setStatus("DONE");

        // When
        TaskModel result = taskController.updateTask(taskId, updated);

        // Then
        assertNotNull(result);
        assertEquals("New", result.getTitle());
        assertEquals("New desc", result.getDescription());
        assertEquals("DONE", result.getStatus());

        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(existing);
    }

    @Test
    void deleteTask_shouldCallRepositoryDelete() {
        Long taskId = 5L;

        taskController.deleteTask(taskId);

        verify(taskRepository).deleteById(taskId);
    }
}
