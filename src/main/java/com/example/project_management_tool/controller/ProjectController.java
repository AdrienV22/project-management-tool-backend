package com.example.project_management_tool.controller;

import com.example.project_management_tool.dto.ProjectMemberRequest;
import com.example.project_management_tool.dto.ProjectMemberResponse;
import com.example.project_management_tool.entity.ProjectMember;
import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.model.ProjectModel;
import com.example.project_management_tool.repository.ProjectMemberRepository;
import com.example.project_management_tool.repository.ProjectRepository;
import com.example.project_management_tool.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectController(ProjectRepository projectRepository,
                             UserRepository userRepository,
                             ProjectMemberRepository projectMemberRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @GetMapping
    public ResponseEntity<List<ProjectModel>> getAllProjects() {
        return ResponseEntity.ok(projectRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectModel> getProjectById(@PathVariable Long id) {
        return projectRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Création d’un projet
     * Validation minimale pour éviter les erreurs 500
     */
    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody ProjectModel project) {

        if (project.getName() == null || project.getName().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Project name is required");
        }

        if (project.getStartDate() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Project startDate is required");
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
                    existing.setClientEmail(updatedProject.getClientEmail());
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

    /**
     * Invite / ajoute un membre au projet (par email) + rôle
     * PUT /api/projects/{projectId}/users
     */
    @PutMapping("/{projectId}/users")
    public ResponseEntity<?> addOrUpdateUserInProject(@PathVariable Long projectId,
                                                      @Valid @RequestBody ProjectMemberRequest request) {

        ProjectModel project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found");
        }

        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User not found for email");
        }

        User user = userOpt.get();

        ProjectMember.ProjectRole role;
        try {
            role = ProjectMember.ProjectRole.valueOf(request.getRole().toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid role. Expected: ADMIN, MEMBRE, OBSERVATEUR");
        }

        ProjectMember member = projectMemberRepository
                .findByProject_IdAndUser_Id(projectId, user.getId())
                .orElseGet(() -> new ProjectMember(project, user, role));

        member.setRole(role);

        ProjectMember saved = projectMemberRepository.save(member);

        ProjectMemberResponse response = new ProjectMemberResponse(
                saved.getUser().getId(),
                saved.getUser().getEmail(),
                saved.getUser().getUsername(),
                saved.getRole().name(),
                saved.getJoinedAt()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Liste des membres d’un projet
     * GET /api/projects/{projectId}/users
     */
    @GetMapping("/{projectId}/users")
    public ResponseEntity<?> listProjectMembers(@PathVariable Long projectId) {

        if (!projectRepository.existsById(projectId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found");
        }

        List<ProjectMemberResponse> members = projectMemberRepository.findByProject_Id(projectId)
                .stream()
                .map(pm -> new ProjectMemberResponse(
                        pm.getUser().getId(),
                        pm.getUser().getEmail(),
                        pm.getUser().getUsername(),
                        pm.getRole().name(),
                        pm.getJoinedAt()
                ))
                .toList();

        return ResponseEntity.ok(members);
    }
}
