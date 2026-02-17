package com.workbuddy.matrix.mapper;

import com.workbuddy.matrix.dto.project.FileNode;
import com.workbuddy.matrix.entity.ProjectFile;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectFileMapper {
    List<FileNode> toListOfFileNode(List<ProjectFile> projectFiles);
}
