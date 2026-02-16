package com.workbuddy.matrix.security;

import com.workbuddy.matrix.enums.ProjectPermission;
import com.workbuddy.matrix.repository.ProjectMembersRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component("security")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SecurityExpressions {
    ProjectMembersRepository projectMembersRepository;
    AuthUtil authUtil;

    public boolean canViewProject(Long projectId){
        return hasPermission(projectId,ProjectPermission.VIEW);
    }

    public boolean canEditProject(Long projectId){
        return hasPermission(projectId,ProjectPermission.EDIT);
    }

    public boolean canDeleteProject(Long projectId){
        long userId = authUtil.getCurrentUserId();
        return hasPermission(projectId,ProjectPermission.DELETE);
    }
    public boolean canManageMembers(Long projectId){
        return hasPermission(projectId,ProjectPermission.MANAGE_MEMBERS);
    }
    public boolean canViewMembers(Long projectId){
        return hasPermission(projectId,ProjectPermission.VIEW_MEMBERS);
    }


    public boolean hasPermission(Long projectId,ProjectPermission projectPermission){
        Long userId = authUtil.getCurrentUserId();

        return projectMembersRepository.findRoleByProjectIdAndUserId(projectId,userId)
                .map(role -> role.getPermissions().contains(projectPermission))
                .orElse(false);
    }
}
