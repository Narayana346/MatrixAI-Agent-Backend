package com.workbuddy.matrixai.workspace_service.service;

import com.workbuddy.matrixai.workspace_service.dto.project.DeployResponse;
import org.jspecify.annotations.Nullable;

public interface DeploymentService {
    @Nullable DeployResponse deploy(Long projectId);
}
