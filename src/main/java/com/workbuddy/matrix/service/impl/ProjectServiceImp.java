package com.workbuddy.matrix.service.impl;

import com.workbuddy.matrix.dto.auth.UserProfileResponse;
import com.workbuddy.matrix.dto.project.ProjectRequest;
import com.workbuddy.matrix.dto.project.ProjectResponse;
import com.workbuddy.matrix.dto.project.ProjectSummaryResponse;
import com.workbuddy.matrix.entity.Project;
import com.workbuddy.matrix.entity.User;
import com.workbuddy.matrix.mapper.ProjectMapper;
import com.workbuddy.matrix.repository.ProjectRepository;
import com.workbuddy.matrix.repository.UserRepository;
import com.workbuddy.matrix.service.ProjectService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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

    @Override
    public ProjectResponse createProject(ProjectRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Project project = Project.builder()
                .name(request.name())
                .owner(user)
                .isPublic(false)
                .build();
        project = projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    public List<ProjectSummaryResponse> getUserProjects(Long userId) {
        return projectRepository.findAllAccessibleByUserId(userId)
                .stream()
                .map(project -> projectMapper.toProjectSummaryResponse(project))
                .collect(Collectors.toList());
    }

    @Override
    public ProjectResponse getUserProjectById(Long id, Long userId) {
        Project project = projectRepository.findByIdAndOwnerId(id, userId).orElseThrow();
        return projectMapper.toProjectResponse(project);
    }

    @Override
    public ProjectResponse updateProject(Long id, ProjectRequest request, Long userId) {
        Project project = projectRepository.findByIdAndOwnerId(id, userId).orElseThrow();
        project.setName(request.name());
        project =  projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    public void softDelete(Long id, Long userId) {
        Project project = projectRepository.findByIdAndOwnerId(id, userId).orElseThrow();
        project.setDeletedAt(Instant.now());
        projectRepository.save(project);
    }
}
