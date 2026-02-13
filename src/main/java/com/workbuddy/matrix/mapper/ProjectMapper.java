package com.workbuddy.matrix.mapper;

import com.workbuddy.matrix.dto.project.ProjectResponse;
import com.workbuddy.matrix.entity.Project;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    ProjectResponse toProjectResponse(Project project);
}
