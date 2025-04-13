package com.example.project_management_tool.controller;

import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.repository.ProjectRepository;
import com.example.project_management_tool.repository.TaskHistoryRepository;
import com.example.project_management_tool.repository.TaskRepository;
import com.example.project_management_tool.repository.UserRepository;
import com.example.project_management_tool.service.EmailService;
import com.example.project_management_tool.entity.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @GetMapping
    public List<TaskModel> getAllTasks() {
        return taskRepository.findAll();
    }

    @PutMapping("/{taskId}")
    public TaskModel updateTask(@RequestParam Long userId, @PathVariable Long taskId,
                                @RequestParam(required = false) String title,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) LocalDate date,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) TaskModel.Priority priority) {

        TaskModel task = taskRepository.findById(taskId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (task == null || user == null) {
            return null;
        }

        if (title != null && !title.isBlank()) {
            task.setTitle(title);
        }
        if (description != null && !description.isBlank()) {
            task.setDescription(description);
        }
        if (date != null && date.isAfter(LocalDate.now())){
            task.setDueDate(date);
        }
        if (status != null && !status.isBlank()) {
            task.setStatus(status);
        }
        if (priority != null) {
            task.setPriority(priority);
        }

        return taskRepository.save(task);
    }

    @GetMapping("/{id}")
    public TaskModel getTaskById(@PathVariable Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    @PostMapping
    public TaskModel createTask(@RequestParam Long userId, @RequestBody TaskModel task) {
        User user = userRepository.findById(userId).orElse(null);
        ProjectModel project = task.getParentProject();

        if (user == null || project == null || project.getId() == null) {
            return null;
        }

        if ((user.getUserRole().equals(User.UserRole.ADMIN) && project.getAdminId().contains(user.getId())
                || (user.getUserRole().equals(User.UserRole.MEMBRE) && project.getUserList().contains(user)))) {
            project.getTaskList().add(task);
        }

        return taskRepository.save(task);
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

    public TaskModel initiateTask(String name, String description, LocalDate date, String status,
                                  TaskModel.Priority priority, Long userId, ProjectModel project) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || project == null || project.getId() == null) {
            return null;
        }
        TaskModel task = new TaskModel(name, description, date, project, status, priority);
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
