package com.workbuddy.matrix.service.impl;

import com.workbuddy.matrix.entity.Project;
import com.workbuddy.matrix.entity.ProjectFile;
import com.workbuddy.matrix.error.ResourceNotFoundException;
import com.workbuddy.matrix.repository.ProjectFileRepository;
import com.workbuddy.matrix.repository.ProjectRepository;
import com.workbuddy.matrix.service.ProjectTemplateService;
import io.minio.*;
import io.minio.messages.Item;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Service
public class ProjectTemplateServiceImpl implements ProjectTemplateService {
    MinioClient minioClient;
    ProjectFileRepository projectFileRepository;
    ProjectRepository projectRepository;


    private static final String TEMPLATE_BUCKET = "project-template";
    private static final String TARGET_BUCKET = "matrix";
    private static final String TEMPLATE_NAME = "react-vite-tailwind-starter";


    @Override
    public void initializeProjectTemplate(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found", projectId.toString()));

        // copy the template files to the project bucket

        try{
            Iterable<Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(TEMPLATE_BUCKET)
                            .prefix(TEMPLATE_NAME)
                            .build()
            );

            List<ProjectFile> fileToSave = new ArrayList<>();

            for(Result<Item> result : objects){
                Item item = result.get();
                String sourceKey = item.objectName();
                String cleanPath = sourceKey.replace(TEMPLATE_NAME +"/","");
                String destinationKey = String.format("%s/%s",project.getId(),cleanPath);

                minioClient.copyObject(CopyObjectArgs.builder()
                                .bucket(TARGET_BUCKET)
                                .object(destinationKey)
                                .source(
                                        CopySource.builder()
                                                .bucket(TEMPLATE_BUCKET)
                                                .object(sourceKey)
                                                .build()
                                )
                                .build()
                );

                ProjectFile projectFile = ProjectFile.builder()
                        .project(project)
                        .path(cleanPath)
                        .minioObjectKey(destinationKey)
                        .build();
                fileToSave.add(projectFile);
            }
            projectFileRepository.saveAll(fileToSave);

        }catch (Exception exception){
            log.error("Failed to initialize project template for project {}",projectId,exception);
            throw new RuntimeException("Failed to initialize project template",exception);
        }
    }
}
