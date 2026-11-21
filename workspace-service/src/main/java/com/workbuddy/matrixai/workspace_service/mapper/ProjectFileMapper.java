package com.workbuddy.matrixai.workspace_service.mapper;

import com.workbuddy.matrixai.common_lib.dto.FileNode;
import com.workbuddy.matrixai.workspace_service.entity.ProjectFile;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectFileMapper {

    List<FileNode> toListOfFileNode(List<ProjectFile> projectFileList);
}
