package com.workbuddy.matrix.service;

import com.workbuddy.matrix.dto.project.FileContentResponse;
import com.workbuddy.matrix.dto.project.FileNode;
import com.workbuddy.matrix.dto.project.FileTreeResponse;

import java.util.List;

public interface ProjectFileService {
    FileTreeResponse getFileTree(Long projectId);

    FileContentResponse getFileContent(Long projectId, String path);

    void saveFile(Long projectId, String filePath, String fileContent);
}
