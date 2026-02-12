package com.workbuddy.matrix.service.impl;

import com.workbuddy.matrix.dto.auth.UserProfileResponse;
import com.workbuddy.matrix.dto.project.ProjectRequest;
import com.workbuddy.matrix.dto.project.ProjectResponse;
import com.workbuddy.matrix.dto.project.ProjectSummaryResponse;
import com.workbuddy.matrix.entity.Project;
import com.workbuddy.matrix.entity.User;
import com.workbuddy.matrix.repository.ProjectRepository;
import com.workbuddy.matrix.repository.UserRepository;
import com.workbuddy.matrix.service.ProjectService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProjectServiceImp implements ProjectService {

    ProjectRepository projectRepository;
    UserRepository userRepository;

    @Override
    public ProjectResponse createProject(ProjectRequest request, Long userId) {
        User user = userRepository.getReferenceById(userId);
        Project project = Project.builder()
                .name(request.name())
                .owner(user)
                .build();
        project = projectRepository.save(project);
        return new ProjectResponse(project.getId(), project.getName(), project.getCreatedAt(), project.getUpdatedAt(),
                new UserProfileResponse(user.getId(), user.getEmail(), user.getName(), user.getAvatarUrl())
        );
    }

    @Override
    public List<ProjectSummaryResponse> getUserProjects(Long userId) {
        return projectRepository.findByOwnerId(userId)
                .stream()
                .map(project ->
                        new ProjectSummaryResponse(project.getId(), project.getName(), project.getCreatedAt(), project.getUpdatedAt())
                ).toList();
    }

    @Override
    public ProjectResponse getUserProjectById(Long id, Long userId) {
        Project project = projectRepository.findByIdAndOwnerId(id, userId).orElseThrow();
        return new ProjectResponse(project.getId(), project.getName(), project.getCreatedAt(), project.getUpdatedAt(),
                new UserProfileResponse(
                        project.getOwner().getId(),
                        project.getOwner().getEmail(),
                        project.getOwner().getName(),
                        project.getOwner().getAvatarUrl())
        );
    }

    @Override
    public ProjectResponse updateProject(Long id, ProjectRequest request, Long userId) {
        Project project = projectRepository.findByIdAndOwnerId(id, userId).orElseThrow();
        project.setName(request.name());
        project =  projectRepository.save(project);
        return new ProjectResponse(project.getId(), project.getName(), project.getCreatedAt(), project.getUpdatedAt(),
                new UserProfileResponse(project.getOwner().getId(), project.getOwner().getEmail(), project.getOwner().getName(), project.getOwner().getAvatarUrl())
        );
    }

    @Override
    public void softDelete(Long id, Long userId) {


    }
}
