package com.workbuddy.matrixai.workspace_service.service;


import com.workbuddy.matrixai.common_lib.dto.FileTreeDto;
import com.workbuddy.matrixai.workspace_service.dto.project.FileContentResponse;

public interface ProjectFileService {
    FileTreeDto getFileTree(Long projectId);

    String getFileContent(Long projectId, String path);

    void saveFile(Long projectId, String filePath, String fileContent);
}
