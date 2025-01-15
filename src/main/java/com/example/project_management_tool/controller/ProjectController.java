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

@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private final ProjectRepository projectRepository;

    public ProjectController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // Endpoint pour récupérer tous les projets
    @CrossOrigin(origins = "http://localhost:4200")  // Appliquer CORS ici aussi si nécessaire
    @GetMapping
    public List<ProjectModel> getAllProjects() {
        return projectRepository.findAll();
    }

    // Initialisation du projet
    @CrossOrigin(origins = "http://localhost:4200")  // Appliquer CORS ici aussi si nécessaire
    @PostMapping("/initialize")
    public ProjectModel InitiateProject(String name, String description, LocalDate startDate, User user) {
        ProjectModel project = new ProjectModel(name, description, startDate);
        if (project.getId() == null) {
            return null;
        }
        project.getAdminId().add(user.getId());
        return createProject(project);
    }

    // Ajouter un utilisateur au projet
    @CrossOrigin(origins = "http://localhost:4200")  // Appliquer CORS ici aussi si nécessaire
    public ProjectModel addUser(User user, ProjectModel project, User.UserRole role, String mail) {
        if (!(user.getUserRole().equals(User.UserRole.ADMIN) || user.getUserRole().equals(User.UserRole.MEMBRE)
                && project.getAdminId().contains(user.getId()))) {
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

    // Définir un rôle pour un utilisateur dans le projet
    @CrossOrigin(origins = "http://localhost:4200")  // Appliquer CORS ici aussi si nécessaire
    public ProjectModel SetRole(User user, ProjectModel project, User.UserRole role, User target) {
        if (!(user.getUserRole().equals(User.UserRole.ADMIN) && project.getAdminId().contains(user.getId()))) {
            return null;
        }
        project.getUserList().stream().filter(u -> u.getId().equals(target.getId())).findFirst().get().setUserRole(role);
        return projectRepository.save(project);
    }

    // Endpoint pour créer un projet
    @CrossOrigin(origins = "http://localhost:4200")  // Appliquer CORS ici aussi si nécessaire
    @PostMapping
    public ProjectModel createProject(@RequestBody ProjectModel project) {
        return projectRepository.save(project);
    }

    // Endpoint pour supprimer un projet
    @CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return ResponseEntity.noContent().build(); // HTTP 204 pour indiquer une suppression réussie
        } else {
            return ResponseEntity.notFound().build(); // HTTP 404 si le projet n'existe pas
        }
    }


    // Endpoint pour récupérer le projet en fonction de l'ID
    @CrossOrigin(origins = "http://localhost:4200")  // Appliquer CORS ici aussi si nécessaire
    @GetMapping("/{id}")
    public ProjectModel getProjectById(@PathVariable Long id) {
        return projectRepository.findById(id).orElse(null);
    }

    // Ajouter une tâche au projet
    @CrossOrigin(origins = "http://localhost:4200")  // Appliquer CORS ici aussi si nécessaire
    public ProjectModel addTask(TaskModel task, ProjectModel project) {
        project.getTaskList().add(task);
        return projectRepository.save(project);
    }

    // Gérer les exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.internalServerError().body("Une erreur est survenue: " + e.getMessage());
    }
}
