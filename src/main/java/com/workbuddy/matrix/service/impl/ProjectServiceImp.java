package com.workbuddy.matrix.service.impl;

import com.workbuddy.matrix.dto.project.ProjectRequest;
import com.workbuddy.matrix.dto.project.ProjectResponse;
import com.workbuddy.matrix.dto.project.ProjectSummaryResponse;
import com.workbuddy.matrix.entity.Project;
import com.workbuddy.matrix.entity.ProjectMemberId;
import com.workbuddy.matrix.entity.ProjectMembers;
import com.workbuddy.matrix.entity.User;
import com.workbuddy.matrix.enums.ProjectRole;
import com.workbuddy.matrix.error.ResourceNotFoundException;
import com.workbuddy.matrix.mapper.ProjectMapper;
import com.workbuddy.matrix.repository.ProjectMembersRepository;
import com.workbuddy.matrix.repository.ProjectRepository;
import com.workbuddy.matrix.repository.UserRepository;
import com.workbuddy.matrix.security.AuthUtil;
import com.workbuddy.matrix.service.ProjectService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Transactional
public class ProjectServiceImp implements ProjectService {

    ProjectRepository projectRepository;
    UserRepository userRepository;
    ProjectMapper projectMapper;
    private final ProjectMembersRepository projectMembersRepository;
    private final AuthUtil authUtil;

    @Override
    public ProjectResponse createProject(ProjectRequest request) {
        Long userId = authUtil.getCurrentUserId();
        User user = userRepository.getReferenceById(userId);

        Project project = Project.builder()
                .name(request.name())
                .isPublic(false)
                .build();
        project = projectRepository.save(project);

        ProjectMemberId projectMemberId = new ProjectMemberId(project.getId(),user.getId());
        ProjectMembers projectMembers = ProjectMembers.builder()
                .id(projectMemberId)
                .role(ProjectRole.OWNER)
                .user(user)
                .acceptedAt(Instant.now())
                .invitedAt(Instant.now())
                .project(project)
                .build();
        projectMembersRepository.save(projectMembers);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    public List<ProjectSummaryResponse> getUserProject() {
        Long userId = authUtil.getCurrentUserId();
        return projectRepository.findAllAccessibleByUserId(userId)
                .stream()
                .map(projectMapper::toProjectSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("@security.canViewProject(#projectId)")
    public ProjectResponse getUserProjectById(Long projectId) {
        Project project = getAccessibleByProjectId(projectId);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project project = getAccessibleByProjectId(id);
        project.setName(request.name());
        project =  projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    @PreAuthorize("@security.canDeleteProject(#projectId)")
    public void softDelete(Long id) {
        Project project = getAccessibleByProjectId(id);
        project.setDeletedAt(Instant.now());
        projectRepository.save(project);
    }

    // internal method
    private Project getAccessibleByProjectId(Long projectId){
        Long userId = authUtil.getCurrentUserId();
        return projectRepository.findAccessibleByProjectId(projectId,userId)
                .orElseThrow(() -> new ResourceNotFoundException("project",projectId.toString()));
    }
}
