package com.example.project_management_tool.controller;

import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.repository.ProjectRepository;
import com.example.project_management_tool.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
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

    /**
     * Récupère l'email de l'utilisateur authentifié.
     * Objectif : éviter tout 500 en cas de SecurityContext vide / auth absente.
     */
    private String getAuthenticatedEmailOrThrow(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }

    // Tous les utilisateurs authentifiés
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ProjectModel> getAllProjects() {
        return projectRepository.findAll();
    }

    // Création projet → ADMIN ou MEMBER
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public ResponseEntity<ProjectModel> createProject(@RequestBody ProjectModel project,
                                                      Authentication authentication) {

        String email = getAuthenticatedEmailOrThrow(authentication);

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Plus logique que 400 : on a un token, mais user non reconnu côté système
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        // On associe l'email client au projet (comme dans ton modèle actuel)
        project.setClientEmail(email);

        ProjectModel createdProject = projectRepository.save(project);

        // 201 Created est plus académique qu'un 200 OK pour une création
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    // Supprimer → ADMIN uniquement
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        if (!projectRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        projectRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Update → ADMIN uniquement
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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

    // Ajouter utilisateur → ADMIN uniquement
    @PutMapping("/{projectId}/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectModel> addUserToProject(@PathVariable Long projectId,
                                                         @RequestParam String userEmail) {

        ProjectModel project = projectRepository.findById(projectId).orElse(null);
        if (project == null) return ResponseEntity.notFound().build();

        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) return ResponseEntity.badRequest().build();

        project.getUserList().add(userOptional.get());
        return ResponseEntity.ok(projectRepository.save(project));
    }

    // Lecture par ID → connecté
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectModel> getProjectById(@PathVariable Long id) {
        return projectRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
