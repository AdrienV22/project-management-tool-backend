package com.example.project_management_tool.controller;

import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.repository.ProjectRepository;
import com.example.project_management_tool.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Autowired
    public ProjectController(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    // Endpoint pour récupérer tous les projets avec le nom du chef de projet (client)
    @GetMapping
    public List<ProjectModel> getAllProjects() {
        List<ProjectModel> projects = projectRepository.findAll();

        // Ajouter le client (nom du chef de projet) pour chaque projet
        projects.forEach(project -> {
            if (!project.getAdminId().isEmpty()) {
                Long adminId = project.getAdminId().get(0);
                userRepository.findById(adminId).ifPresent(user -> project.setClient(user.getUsername()));
            }
        });

        return projects;
    }

    // Initialisation d'un nouveau projet
    @PostMapping("/initialize")
    public ResponseEntity<ProjectModel> initiateProject(@RequestParam String name,
                                                        @RequestParam String description,
                                                        @RequestParam LocalDate startDate,
                                                        @RequestBody User user) {
        ProjectModel project = new ProjectModel(name, description, startDate);
        project.getAdminId().add(user.getId());
        ProjectModel createdProject = projectRepository.save(project);
        return ResponseEntity.ok(createdProject);
    }

    // Ajouter un utilisateur au projet
    @PutMapping("/{projectId}/users")
    public ResponseEntity<ProjectModel> addUserToProject(@PathVariable Long projectId,
                                                         @RequestParam String userEmail,
                                                         @RequestParam User.UserRole role) {
        ProjectModel project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(null);
        }

        user.setUserRole(role);
        project.getUserList().add(user);
        ProjectModel updatedProject = projectRepository.save(project);
        return ResponseEntity.ok(updatedProject);
    }

    // Endpoint pour créer un projet
    @PostMapping
    public ResponseEntity<ProjectModel> createProject(@RequestBody ProjectModel project) {
        ProjectModel createdProject = projectRepository.save(project);
        return ResponseEntity.ok(createdProject);
    }

    // Endpoint pour supprimer un projet
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Endpoint pour récupérer un projet par ID
    @GetMapping("/{id}")
    public ResponseEntity<ProjectModel> getProjectById(@PathVariable Long id) {
        return projectRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Ajouter une tâche à un projet
    @PutMapping("/{projectId}/tasks")
    public ResponseEntity<ProjectModel> addTaskToProject(@PathVariable Long projectId, @RequestBody TaskModel task) {
        ProjectModel project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        project.getTaskList().add(task);
        ProjectModel updatedProject = projectRepository.save(project);
        return ResponseEntity.ok(updatedProject);
    }

    // Endpoint pour mettre à jour un projet
    @PutMapping("/{id}")
    public ResponseEntity<ProjectModel> updateProject(@PathVariable Long id, @RequestBody ProjectModel updatedProject) {
        return projectRepository.findById(id)
                .map(existingProject -> {
                    existingProject.setName(updatedProject.getName());
                    existingProject.setDescription(updatedProject.getDescription());
                    existingProject.setStartDate(updatedProject.getStartDate());
                    existingProject.setStatut(updatedProject.getStatut());
                    ProjectModel savedProject = projectRepository.save(existingProject);
                    return ResponseEntity.ok(savedProject);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Gérer les exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.internalServerError().body("Une erreur est survenue: " + e.getMessage());
    }
}
