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
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
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
            if (project.getClientEmail() != null) {
                // Récupérer l'utilisateur (chef de projet) via l'email
                Optional<User> clientOptional = userRepository.findByEmail(project.getClientEmail());
                if (clientOptional.isPresent()) {
                    User client = clientOptional.get(); // Extraire l'utilisateur de l'Optional
                    project.setClientEmail(client.getEmail()); // Assigner l'email du chef de projet
                }
            }
        });

        return projects;
    }

    // Initialiser un projet en récupérant le client par email
    @PostMapping("/initialize")
    public ResponseEntity<ProjectModel> initiateProject(@RequestParam String name,
                                                        @RequestParam String description,
                                                        @RequestParam LocalDate startDate,
                                                        @RequestParam String clientEmail) {
        // Récupérer le client (chef de projet) par email
        Optional<User> clientOptional = userRepository.findByEmail(clientEmail);
        if (clientOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(null);  // Si le client n'est pas trouvé
        }

        User client = clientOptional.get(); // Extraire l'utilisateur de l'Optional

        ProjectModel project = new ProjectModel(name, description, startDate);
        project.setClientEmail(client.getEmail());  // Assigner l'email du chef de projet
        ProjectModel createdProject = projectRepository.save(project);
        return ResponseEntity.ok(createdProject);
    }

    // Ajouter un utilisateur à un projet
    @PutMapping("/{projectId}/users")
    public ResponseEntity<ProjectModel> addUserToProject(@PathVariable Long projectId,
                                                         @RequestParam String userEmail,
                                                         @RequestParam User.UserRole role) {
        ProjectModel project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        // Récupérer l'utilisateur par son email
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        User user = userOptional.get();  // L'utilisateur est de type UserEntity
        user.setUserRole(role); // Mettre à jour le rôle de l'utilisateur
        project.getUserList().add(user); // Ajouter l'utilisateur à la liste des utilisateurs du projet

        ProjectModel updatedProject = projectRepository.save(project);
        return ResponseEntity.ok(updatedProject);
    }


    // Créer un projet
    @PostMapping
    public ResponseEntity<ProjectModel> createProject(@RequestBody ProjectModel project) {
        // Récupérer et associer le client (chef de projet) par email
        if (project.getClientEmail() != null) {
            Optional<User> clientOptional = userRepository.findByEmail(project.getClientEmail());
            if (clientOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(null);  // Si le client n'est pas trouvé
            }

            User client = clientOptional.get();  // Extraire l'utilisateur de l'Optional
            project.setClientEmail(client.getEmail());  // Assigner l'email du chef de projet
        }

        ProjectModel createdProject = projectRepository.save(project);
        return ResponseEntity.ok(createdProject);
    }

    // Supprimer un projet
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Récupérer un projet par ID
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

    // Mettre à jour un projet
    @PutMapping("/{id}")
    public ResponseEntity<ProjectModel> updateProject(@PathVariable Long id, @RequestBody ProjectModel updatedProject) {
        return projectRepository.findById(id)
                .map(existingProject -> {
                    existingProject.setName(updatedProject.getName());
                    existingProject.setDescription(updatedProject.getDescription());
                    existingProject.setStartDate(updatedProject.getStartDate());
                    existingProject.setStatut(updatedProject.getStatut());
                    existingProject.setEndDate(updatedProject.getEndDate()); // Mise à jour de la date de fin
                    existingProject.setClientEmail(updatedProject.getClientEmail()); // Mise à jour de l'email du chef de projet
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
