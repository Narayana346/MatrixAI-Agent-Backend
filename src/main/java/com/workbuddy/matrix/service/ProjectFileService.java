package com.workbuddy.matrix.service;

import com.workbuddy.matrix.dto.project.FileContentResponse;
import com.workbuddy.matrix.dto.project.FileNode;

import java.util.List;

public interface ProjectFileService {
    List<FileNode> getFileTree(Long projectId, Long userId);

    FileContentResponse getFileContent(Long projectId, String path, Long userId);

    void saveFile(Long userId,Long projectId, String filePath, String fileContent);
}
