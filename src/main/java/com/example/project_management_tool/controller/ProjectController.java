package com.example.project_management_tool.controller;

import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")  // Permet au frontend de se connecter depuis ce port
@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private final ProjectRepository projectRepository;

    public ProjectController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // Endpoint pour récupérer tous les projets
    @GetMapping
    public List<ProjectModel> getAllProjects() {
        return projectRepository.findAll();
    }

    // Initialisation du projet
    @PostMapping("/initialize")
    public ProjectModel InitiateProject(String name, String description, LocalDate startDate, User user) {
        ProjectModel project = new ProjectModel(name, description, startDate);
        if (project.getId() == null) {
            return null;
        }
        project.getAdminId().add(user.getId());
        return createProject(project);
    }

    // Add user
    public ProjectModel addUser(User user, ProjectModel project, User.UserRole role, String mail) {
        if (!(user.getUserRole().equals(User.UserRole.ADMIN) ||user.getUserRole().equals(User.UserRole.MEMBRE)
                && project.getAdminId().contains(user.getId())))
        {
            return null;
        }
        UserController userController = new UserController();
        List<User> users = userController.getAllUsers();
        User foundUser = users.stream().filter(u -> u.getEmail().equals(mail)).findFirst().orElse(null);
        assert foundUser != null;
        foundUser.setUserRole(role);
        project.getUserList().add(foundUser);
        return projectRepository.save(project);
    }

    public ProjectModel SetRole(User user, ProjectModel project, User.UserRole role, User target) {
        if (!(user.getUserRole().equals(User.UserRole.ADMIN) && project.getAdminId().contains(user.getId())))
        {
            return null;
        }
        project.getUserList().stream().filter(u -> u.getId().equals(target.getId())).findFirst().get().setUserRole(role);
        return projectRepository.save(project);
    }

    // Endpoint pour créer un projet
    @PostMapping
    public ProjectModel createProject(@RequestBody ProjectModel project) {
        return projectRepository.save(project);
    }


    // Endpoint pour récupérer le projet en fonction de l'ID
    @GetMapping("/{id}")
    public ProjectModel getProjectById(@PathVariable Long id) {
        return projectRepository.findById(id).orElse(null);
    }


    public ProjectModel addTask(TaskModel task, ProjectModel project) {
        project.getTaskList().add(task);
        return projectRepository.save(project);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.internalServerError().body("Une erreur est survenue: " + e.getMessage());
    }
}