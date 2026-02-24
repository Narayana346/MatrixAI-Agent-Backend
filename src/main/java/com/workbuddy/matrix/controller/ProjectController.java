package com.workbuddy.matrix.controller;

import com.workbuddy.matrix.dto.project.ProjectRequest;
import com.workbuddy.matrix.dto.project.ProjectResponse;
import com.workbuddy.matrix.dto.project.ProjectSummaryResponse;
import com.workbuddy.matrix.security.AuthUtil;
import com.workbuddy.matrix.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectSummaryResponse>> getMyProjects() {
        return ResponseEntity.ok(projectService.getUserProject());
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectSummaryResponse> getProjectById(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getUserProjectById(projectId));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestBody @Valid ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @PatchMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long projectId, @RequestBody @Valid ProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(projectId, request));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        projectService.softDelete(projectId);
        return ResponseEntity.noContent().build();
    }
}
