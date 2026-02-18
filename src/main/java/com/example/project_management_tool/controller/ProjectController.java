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
     * ✅ Option A — Invite un membre par email + attribue un rôle.
     * Endpoint conservé : PUT /api/projects/{projectId}/users
     *
     * Body attendu :
     * { "email": "x@y.com", "role": "MEMBRE" }
     * role est optionnel -> MEMBRE par défaut
     */
    @PutMapping("/{projectId}/users")
    public ResponseEntity<?> addOrUpdateMember(@PathVariable Long projectId,
                                               @Valid @RequestBody ProjectMemberRequest request) {

        ProjectModel project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found");
        }

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User not found for email: " + request.getEmail());
        }

        ProjectMember.ProjectRole role =
                (request.getRole() == null) ? ProjectMember.ProjectRole.MEMBRE : request.getRole();

        ProjectMember member = projectMemberRepository
                .findByProject_IdAndUser_Id(projectId, user.getId())
                .orElseGet(ProjectMember::new);

        member.setProject(project);
        member.setUser(user);
        member.setRole(role);

        ProjectMember saved = projectMemberRepository.save(member);

        ProjectMemberResponse response = new ProjectMemberResponse(
                saved.getUser().getId(),
                saved.getUser().getUsername(),
                saved.getUser().getEmail(),
                saved.getRole(),
                saved.getJoinedAt()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * ✅ Liste les membres du projet + leurs rôles
     * GET /api/projects/{projectId}/users
     */
    @GetMapping("/{projectId}/users")
    public ResponseEntity<?> listMembers(@PathVariable Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found");
        }

        List<ProjectMemberResponse> members = projectMemberRepository.findByProject_Id(projectId)
                .stream()
                .map(pm -> new ProjectMemberResponse(
                        pm.getUser().getId(),
                        pm.getUser().getUsername(),
                        pm.getUser().getEmail(),
                        pm.getRole(),
                        pm.getJoinedAt()
                ))
                .toList();

        return ResponseEntity.ok(members);
    }
}
