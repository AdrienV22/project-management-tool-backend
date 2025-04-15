package com.example.project_management_tool.controller;

import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.repository.ProjectRepository;
import com.example.project_management_tool.repository.TaskHistoryRepository;
import com.example.project_management_tool.repository.TaskRepository;
import com.example.project_management_tool.repository.UserRepository;
import com.example.project_management_tool.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // ✅ Constructeur ajouté pour les tests unitaires
    public TaskController(TaskRepository taskRepository,
                          TaskHistoryRepository taskHistoryRepository,
                          ProjectRepository projectRepository,
                          UserRepository userRepository,
                          EmailService emailService) {
        this.taskRepository = taskRepository;
        this.taskHistoryRepository = taskHistoryRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @GetMapping
    public List<TaskModel> getAllTasks() {
        return taskRepository.findAll();
    }

    @GetMapping("/{id}")
    public TaskModel getTaskById(@PathVariable Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    @PostMapping
    public TaskModel createTask(@RequestParam Long userId, @RequestBody TaskModel task) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || task.getParentProject() == null || task.getParentProject().getId() == null) return null;

        ProjectModel managedProject = projectRepository.findById(task.getParentProject().getId()).orElse(null);
        if (managedProject == null) return null;

        task.setParentProject(managedProject);

        if ((user.getUserRole() == User.UserRole.ADMIN && managedProject.getAdminId().contains(user.getId()))
                || (user.getUserRole() == User.UserRole.MEMBRE && managedProject.getUserList().contains(user))) {
            managedProject.getTaskList().add(task);
            projectRepository.save(managedProject);
        }

        return taskRepository.save(task);
    }

    @PutMapping("/{taskId}")
    public TaskModel updateTask(@PathVariable Long taskId, @RequestBody TaskModel updatedTask) {
        TaskModel existingTask = taskRepository.findById(taskId).orElse(null);
        if (existingTask == null) return null;

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setDueDate(updatedTask.getDueDate());
        existingTask.setStatus(updatedTask.getStatus());
        existingTask.setPriority(updatedTask.getPriority());

        return taskRepository.save(existingTask);
    }

    @DeleteMapping("/{taskId}")
    public void deleteTask(@PathVariable Long taskId) {
        taskRepository.deleteById(taskId);
    }

    public TaskModel addUser(Long userId, TaskModel task, User.UserRole role, User target) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || task == null) return null;

        if (!(user.getUserRole().equals(User.UserRole.ADMIN) ||
                user.getUserRole().equals(User.UserRole.MEMBRE) &&
                        task.getParentProject().getAdminId().contains(user.getId()))) {
            return null;
        }

        task.getParentProject().getUserList().add(target);

        if (!emailService.testMailSender()) return null;

        emailService.sendEmail(target);
        return taskRepository.save(task);
    }

    public TaskModel initiateTask(String name, String description, java.time.LocalDate date, String status,
                                  TaskModel.Priority priority, Long userId, ProjectModel project) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || project == null || project.getId() == null) {
            return null;
        }
        TaskModel task = new TaskModel(name, description, date, project, status, priority, userId);
        return createTask(userId, task);
    }

    public TaskModel visualizeTask(Long userId, TaskModel task) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || task.getParentProject().getId() == null || !user.getProjectList().contains(task.getParentProject()))
            return null;
        return task;
    }

    public List<TaskModel> visualizeTasks(Long userId, ProjectModel project, List<String> statusList) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || project.getId() == null || !user.getProjectList().contains(project))
            return null;
        return project.getTaskList().stream()
                .filter(task -> statusList.contains(task.getStatus()))
                .collect(Collectors.toList());
    }
}
