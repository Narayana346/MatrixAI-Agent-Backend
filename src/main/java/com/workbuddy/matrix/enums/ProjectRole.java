package com.workbuddy.matrix.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import static com.workbuddy.matrix.enums.ProjectPermission.*;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public enum ProjectRole {
    EDITOR(VIEW,EDIT,VIEW_MEMBERS,DELETE),
    VIEWER(VIEW,VIEW_MEMBERS),
    OWNER(VIEW,EDIT,DELETE,VIEW_MEMBERS,MANAGE_MEMBERS);

    ProjectRole(ProjectPermission... permissions){
        this.permissions = Set.of(permissions);
    }

    private final Set<ProjectPermission> permissions;
}
