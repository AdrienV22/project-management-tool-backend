package com.example.project_management_tool.controller;

import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.repository.ProjectRepository;
import com.example.project_management_tool.repository.TaskHistoryRepository;
import com.example.project_management_tool.repository.TaskRepository;
import com.example.project_management_tool.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.project_management_tool.entity.User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200")  // Permet au frontend de se connecter depuis ce port
@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    @Autowired
    private ProjectRepository projectRepository;

    // Endpoint pour récupérer toutes les tâches
    @GetMapping
    public List<TaskModel> getAllTasks() {
        return taskRepository.findAll();
    }

    @PutMapping("/{taskId}")
    public TaskModel updateTask(@RequestParam User user, @PathVariable Long taskId,
                                @RequestParam(required = false) String title,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) LocalDate date,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) TaskModel.Priority priority) {
        TaskModel task = taskRepository.findById(taskId).orElse(null);

        if (task == null) {
            return null;
        }
        TaskHistoryController taskHistoryController = new TaskHistoryController();
        if (title != null && !title.isBlank()) {
            taskHistoryController.recordHistory(getTaskById(taskId), "title", task.getTitle(),
                    title, user.getUsername());
            task.setTitle(title);
        }
        if (description != null && !description.isBlank()) {
            taskHistoryController.recordHistory(getTaskById(taskId), "description",
                    task.getTitle(), title, user.getUsername());
            task.setDescription(description);
        }
        if (date != null && date.isAfter(LocalDate.now())){
            taskHistoryController.recordHistory(getTaskById(taskId), "date", task.getTitle(),
                    title, user.getUsername());
            task.setDueDate(date);
        }
        if (status != null && !status.isBlank()) {
            taskHistoryController.recordHistory(getTaskById(taskId), "status", task.getTitle(),
                    title, user.getUsername());
            task.setStatus(status);
        }
        if (priority != null) {
            taskHistoryController.recordHistory(getTaskById(taskId), "priority", task.getTitle(),
                    title, user.getUsername());
            task.setPriority(priority);
        }
        return taskRepository.save(task);
    }

    // Endpoint pour récupérer une tâche par son ID
    @GetMapping("/{id}")
    public TaskModel getTaskById(@PathVariable Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public TaskModel addProject(TaskModel task, ProjectModel project) {
        task.setParentProject(project);
        return taskRepository.save(task);
    }

    // Endpoint pour créer une tâche
    @PostMapping
    public TaskModel createTask(@Valid ProjectModel project,  @RequestParam User user, @RequestParam TaskModel task) {
        if (project.getId() == null) {
            return null;
        }
        if ((user.getUserRole().equals(User.UserRole.ADMIN) && project.getAdminId().contains(user.getId())
                || (user.getUserRole().equals(User.UserRole.MEMBRE) && project.getUserList().contains(user))))
        {
            project.getTaskList().add(task);
        }
        return taskRepository.save(task);
    }

    public TaskModel addUser(User user, TaskModel task, User.UserRole role, User target) {
        if (!(user.getUserRole().equals(User.UserRole.ADMIN) ||user.getUserRole().equals(User.UserRole.MEMBRE)
                && task.getParentProject().getAdminId().contains(user.getId())))
        {
            return null;
        }
        task.getParentProject().getUserList().add(target);
        if (!EmailService.testMailSender())
            return null;
        EmailService.sendEmail(target);
        return taskRepository.save(task);
    }

    public TaskModel InitiateTask(String name, String description, LocalDate date, String status,
                                  TaskModel.Priority priority, @RequestParam User user, @Valid ProjectModel project) {
        if (project.getId() == null) {
            return null;
        }
        TaskModel task = new TaskModel(name, description, date, project, status, priority);
        return createTask(project, user, task);
    }

    public TaskModel TaskVisualisation(@RequestParam User user, @Valid TaskModel task) {
        if (task.getParentProject().getId() == null || !user.getProjectList().contains(task.getParentProject()))
            return null;
        return task;
    }

    public List<TaskModel> TasksVisualization(@RequestParam User user, @RequestParam ProjectModel project,
                                              List<String> statusList) {
        if (project.getId() == null ||  !user.getProjectList().contains(project))
            return null;
        return project.getTaskList().stream()
                .filter(task -> statusList.contains(task.getStatus()))
                .collect(Collectors.toList());
    }
}