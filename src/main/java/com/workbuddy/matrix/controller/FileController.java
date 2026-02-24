package com.workbuddy.matrix.controller;

import com.workbuddy.matrix.dto.project.FileContentResponse;
import com.workbuddy.matrix.dto.project.FileNode;
import com.workbuddy.matrix.dto.project.FileTreeResponse;
import com.workbuddy.matrix.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project/{projectId}/files")
@RequiredArgsConstructor
public class FileController {
    private final ProjectFileService projectFileService;

    @GetMapping
    public ResponseEntity<FileTreeResponse> getFileTree(@PathVariable Long projectId){
        return ResponseEntity.ok(projectFileService.getFileTree(projectId));
    }

    @GetMapping("/content")
    public ResponseEntity<FileContentResponse> getFile(@PathVariable Long projectId, @RequestParam String path){
        return ResponseEntity.ok(projectFileService.getFileContent(projectId,path));
    }
}
