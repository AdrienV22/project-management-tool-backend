package com.example.project_management_tool.controller;

import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.repository.ProjectRepository;
import com.example.project_management_tool.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectController(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllProjects() {
        return ResponseEntity.ok(projectRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectModel> getProjectById(@PathVariable Long id) {
        return projectRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Création de projet.
     * Justification : la sécurité n'est pas requise, donc pas d'Authentication/SecurityContext.
     */
    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody ProjectModel project) {
        if (project.getName() == null || project.getName().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Project name is required");
        }

        ProjectModel createdProject = projectRepository.save(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectModel> updateProject(@PathVariable Long id,
                                                      @RequestBody ProjectModel updatedProject) {

        return projectRepository.findById(id)
                .map(existing -> {
                    existing.setName(updatedProject.getName());
                    existing.setDescription(updatedProject.getDescription());
                    existing.setStartDate(updatedProject.getStartDate());
                    existing.setEndDate(updatedProject.getEndDate());
                    existing.setStatut(updatedProject.getStatut());
                    return ResponseEntity.ok(projectRepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        if (!projectRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        projectRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{projectId}/users")
    public ResponseEntity<ProjectModel> addUserToProject(@PathVariable Long projectId,
                                                         @RequestParam String userEmail) {

        ProjectModel project = projectRepository.findById(projectId).orElse(null);
        if (project == null) return ResponseEntity.notFound().build();

        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) return ResponseEntity.badRequest().build();

        project.getUserList().add(userOptional.get());
        return ResponseEntity.ok(projectRepository.save(project));
    }
}
