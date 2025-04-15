package com.example.project_management_tool.controller;

import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.repository.ProjectRepository;
import com.example.project_management_tool.repository.TaskHistoryRepository;
import com.example.project_management_tool.repository.TaskRepository;
import com.example.project_management_tool.repository.UserRepository;
import com.example.project_management_tool.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TaskControllerTest {

    private TaskRepository taskRepository;
    private TaskHistoryRepository taskHistoryRepository;
    private ProjectRepository projectRepository;
    private UserRepository userRepository;
    private EmailService emailService;
    private TaskController taskController;

    @BeforeEach
    public void setup() {
        // Création des mocks
        taskRepository = mock(TaskRepository.class);
        taskHistoryRepository = mock(TaskHistoryRepository.class);
        projectRepository = mock(ProjectRepository.class);
        userRepository = mock(UserRepository.class);
        emailService = mock(EmailService.class);

        // Injection des mocks via le constructeur
        taskController = new TaskController(taskRepository, taskHistoryRepository, projectRepository, userRepository, emailService);
    }

    @Test
    public void testCreateTaskSuccess() {
        Long userId = 1L;
        Long projectId = 100L;

        User user = new User();
        user.setId(userId);
        user.setUserRole(User.UserRole.ADMIN);

        ProjectModel project = new ProjectModel();
        project.setId(projectId);
        project.getAdminId().add(userId);

        TaskModel task = new TaskModel("Tâche test", "Description", LocalDate.now().plusDays(5),
                project, "En attente", TaskModel.Priority.MOYENNE, userId);
        task.setParentProject(project);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(taskRepository.save(any(TaskModel.class))).thenReturn(task);

        TaskModel createdTask = taskController.createTask(userId, task);

        assertNotNull(createdTask);
        assertEquals("Tâche test", createdTask.getTitle());
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    public void testUpdateTaskSuccess() {
        Long taskId = 1L;

        TaskModel existingTask = new TaskModel();
        existingTask.setId(taskId);
        existingTask.setTitle("Ancien titre");
        existingTask.setDescription("Ancienne description");
        existingTask.setDueDate(LocalDate.now().plusDays(2));
        existingTask.setStatus("En attente");
        existingTask.setPriority(TaskModel.Priority.MOYENNE);

        TaskModel updatedTask = new TaskModel();
        updatedTask.setTitle("Nouveau titre");
        updatedTask.setDescription("Nouvelle description");
        updatedTask.setDueDate(LocalDate.now().plusDays(5));
        updatedTask.setStatus("Terminé");
        updatedTask.setPriority(TaskModel.Priority.HAUTE);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(TaskModel.class))).thenAnswer(i -> i.getArgument(0));

        TaskModel result = taskController.updateTask(taskId, updatedTask);

        assertNotNull(result);
        assertEquals("Nouveau titre", result.getTitle());
        assertEquals("Nouvelle description", result.getDescription());
        assertEquals("Terminé", result.getStatus());
        assertEquals(TaskModel.Priority.HAUTE, result.getPriority());
        verify(taskRepository, times(1)).save(existingTask);
    }

    @Test
    public void testDeleteTaskSuccess() {
        Long taskId = 1L;

        taskController.deleteTask(taskId);

        verify(taskRepository, times(1)).deleteById(taskId);
    }
}
