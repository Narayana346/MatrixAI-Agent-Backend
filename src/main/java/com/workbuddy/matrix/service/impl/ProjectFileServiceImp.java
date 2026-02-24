package com.workbuddy.matrix.service.impl;

import com.workbuddy.matrix.dto.project.FileContentResponse;
import com.workbuddy.matrix.dto.project.FileNode;
import com.workbuddy.matrix.dto.project.FileTreeResponse;
import com.workbuddy.matrix.entity.Project;
import com.workbuddy.matrix.entity.ProjectFile;
import com.workbuddy.matrix.entity.User;
import com.workbuddy.matrix.error.ResourceNotFoundException;
import com.workbuddy.matrix.mapper.ProjectFileMapper;
import com.workbuddy.matrix.repository.ProjectFileRepository;
import com.workbuddy.matrix.repository.ProjectRepository;
import com.workbuddy.matrix.repository.UserRepository;
import com.workbuddy.matrix.security.AuthUtil;
import com.workbuddy.matrix.service.ProjectFileService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectFileServiceImp implements ProjectFileService {
    private final UserRepository userRepository;
    private final ProjectFileRepository projectFileRepository;
    private final ProjectRepository projectRepository;
    private final MinioClient minioClient;
    private final ProjectFileMapper projectFileMapper;

    @Value("${minio.bucket}")
    private String bucketName;

    @Override
    public FileTreeResponse getFileTree(Long projectId) {
        List<ProjectFile> projectFiles = projectFileRepository.findByProjectId(projectId);
        List<FileNode> fileNodes = projectFileMapper.toListOfFileNode(projectFiles);
        return new FileTreeResponse(fileNodes);
    }

    @Override
    public FileContentResponse getFileContent(Long projectId, String path) {
        String objectKey = projectId + "/" + path;
        try{
            InputStream fileContentStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build()
            );
            String fileContent = new String(fileContentStream.readAllBytes(),StandardCharsets.UTF_8);
            return new FileContentResponse(path,fileContent);
        }catch (Exception exception){
            log.error("Failed to get file content for project {} and path {}",projectId,path,exception);
            throw new RuntimeException("Failed to get file content",exception);
        }
    }

    @Override
    public void saveFile(Long projectId, String filePath, String fileContent) {
        // Save the file Metadata in postgres and save file content in minio .
        log.info("Saving file {} for project {}",filePath,projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found",projectId.toString()));

        String cleanPath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
        String objectKey = String.format("%s/%s",project.getId(),cleanPath);

        try{
            byte[] fileContentBytes = fileContent.getBytes(StandardCharsets.UTF_8);
            InputStream fileContentStream = new ByteArrayInputStream(fileContentBytes);

            // Save the file content in minio
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .stream(fileContentStream,fileContentBytes.length, -1)
                    .contentType(determineContentType(filePath))
                    .build()
            );
            // Save the file metadata
            ProjectFile projectFile = projectFileRepository.findByProjectIdAndPath(projectId,cleanPath)
                    .orElseGet(() -> ProjectFile.builder()
                    .project(project)
                    .path(cleanPath)
                    .minioObjectKey(objectKey)
                    .createdAt(Instant.now())
                    .build()
            );

            projectFile.setUpdatedAt(Instant.now());
            projectFileRepository.save(projectFile);

            log.info("File {} saved successfully for project {} and object key {}",filePath,projectId,objectKey);

        }catch (Exception exception){
            log.error("Failed to save file {}/{}",filePath,projectId,exception);
            throw new RuntimeException("Failed to save file",exception);
        }

    }

    private String determineContentType(String filePath){
        String type = URLConnection.guessContentTypeFromName(filePath);
        if(type == null) return type;

        if(filePath.endsWith(".jsx") ||
                filePath.endsWith(".ts") ||
                filePath.endsWith(".tsx") ||
                filePath.endsWith(".js")
        ) return "text/javascript";

        if(filePath.endsWith(".json")) return "application/json";
        if (filePath.endsWith(".css")) return "text/css";
        if (filePath.endsWith(".html")) return "text/html";
        return "text/plain";
    }
}
