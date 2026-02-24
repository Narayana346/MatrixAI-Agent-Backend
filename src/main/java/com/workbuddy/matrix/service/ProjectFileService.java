package com.workbuddy.matrix.service;

import com.workbuddy.matrix.dto.project.FileContentResponse;
import com.workbuddy.matrix.dto.project.FileNode;

import java.util.List;

public interface ProjectFileService {
    List<FileNode> getFileTree(Long projectId);

    FileContentResponse getFileContent(Long projectId, String path);

    void saveFile(Long projectId, String filePath, String fileContent);
}
